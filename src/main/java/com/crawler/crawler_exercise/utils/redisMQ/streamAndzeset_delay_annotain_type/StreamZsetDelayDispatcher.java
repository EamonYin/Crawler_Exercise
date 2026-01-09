package com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
@ConditionalOnProperty(name = "redis.mq.mode", havingValue = "streamZsetDelay")
public class StreamZsetDelayDispatcher {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DelayRedisStreamListenerRegistrar listenerRegistrar;

    @Value("${redis.mq.delay.batch-size:100}")
    private long batchSize;

    @Scheduled(fixedDelayString = "${redis.mq.delay.poll-interval-ms:1000}")
    public void dispatchDelayMessages() {
        Set<String> streamKeys = listenerRegistrar.getStreamKeys();
        if (streamKeys.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        for (String streamKey : streamKeys) {
            String delayKey = StreamZsetDelayKeys.delayQueueKey(streamKey);
            drainDelayQueue(delayKey, streamKey, now);
        }
    }

    private void drainDelayQueue(String delayKey, String streamKey, long now) {
        long limit = Math.max(1L, batchSize);
        while (true) {
            Set<String> payloads = redisTemplate.opsForZSet().rangeByScore(delayKey, 0, now, 0, limit);
            if (payloads == null || payloads.isEmpty()) {
                return;
            }
            for (String payload : payloads) {
                String message = StreamZsetDelayKeys.decodePayload(payload);
                try {
                    Map<String, String> body = new HashMap<>();
                    body.put("data", message);
                    redisTemplate.opsForStream()
                            .add(StreamRecords.mapBacked(body).withStreamKey(streamKey));
                    redisTemplate.opsForZSet().remove(delayKey, payload);
                } catch (Exception e) {
                    log.error("Delay dispatch failed. streamKey={}, delayKey={}", streamKey, delayKey, e);
                }
            }
            if (payloads.size() < limit) {
                return;
            }
        }
    }
}
