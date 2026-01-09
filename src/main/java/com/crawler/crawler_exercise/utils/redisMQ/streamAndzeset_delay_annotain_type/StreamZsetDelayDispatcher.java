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
// 与 annotain_type 不同：增加调度器，把延时 ZSET 的到期消息转投到 Stream。
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

    /**
     * drainDelayQueue 的作用：
     * 从指定的延时 ZSET 里批量拉出“已到期”的成员（score <= now），
     * 逐条投递到对应的 Stream（XADD），
     * 投递成功后再从 ZSET 删除，直到本轮没有更多到期消息或数量不足 batchSize 为止。
     */
    private void drainDelayQueue(String delayKey, String streamKey, long now) {
        long limit = Math.max(1L, batchSize);
        while (true) {
            // 从延时 ZSET 中批量取出到期消息（score <= now）
            // 对应 Redis 命令：ZRANGEBYSCORE {delayKey} 0 {now} LIMIT 0 {limit}
            Set<String> payloads = redisTemplate.opsForZSet().rangeByScore(delayKey, 0, now, 0, limit);
            if (payloads == null || payloads.isEmpty()) {
                return;
            }
            for (String payload : payloads) {
                String message = StreamZsetDelayKeys.decodePayload(payload);
                try {
                    Map<String, String> body = new HashMap<>();
                    body.put("data", message);
                    // 投递到 Stream（消费侧用 XREADGROUP 消费）
                    // 对应 Redis 命令：XADD {streamKey} * data "{message}"
                    redisTemplate.opsForStream()
                            .add(StreamRecords.mapBacked(body).withStreamKey(streamKey));
                    // 投递成功后，从延时 ZSET 移除该条消息
                    // 对应 Redis 命令：ZREM {delayKey} "{payload}"
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
