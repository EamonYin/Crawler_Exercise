package com.crawler.crawler_exercise.utils.redisMQ.zset_delay_annotain_type;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnnotainDelayProducer {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void send(String queueKey, String message) {
        send(queueKey, message, 0L);
    }

    public void send(String queueKey, String message, long delayMillis) {
        long score = System.currentTimeMillis() + Math.max(0L, delayMillis);
        redisTemplate.opsForZSet().add(queueKey, message, score);
    }
}
