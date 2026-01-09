package com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "redis.mq.mode", havingValue = "streamZsetDelay")
public class DelayAnnotainProducer {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void send(String streamKey, String message) {
        send(streamKey, message, 0L);
    }

    public void send(String streamKey, String message, long delayMillis) {
        // 与 annotain_type 不同：这里先写入延时 ZSET，由调度器到期再 XADD 到 Stream。
        String delayKey = StreamZsetDelayKeys.delayQueueKey(streamKey);
        String payload = StreamZsetDelayKeys.encodePayload(message);
        long score = System.currentTimeMillis() + Math.max(0L, delayMillis);
        redisTemplate.opsForZSet().add(delayKey, payload, score);
    }

}
