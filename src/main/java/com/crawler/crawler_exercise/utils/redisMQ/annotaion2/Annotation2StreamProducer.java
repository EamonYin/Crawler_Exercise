package com.crawler.crawler_exercise.utils.redisMQ.annotaion2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 注解2模式的发送入口
 */
@Component
public class Annotation2StreamProducer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void send(String streamKey, Map<String, String> message) {
        stringRedisTemplate.opsForStream().add(streamKey, message);
    }
}
