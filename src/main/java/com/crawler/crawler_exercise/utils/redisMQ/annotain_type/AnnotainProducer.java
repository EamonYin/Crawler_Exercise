package com.crawler.crawler_exercise.utils.redisMQ.annotain_type;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AnnotainProducer {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void send(String streamKey, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("data", message);

        redisTemplate.opsForStream()
                .add(StreamRecords.mapBacked(body).withStreamKey(streamKey));
    }

}
