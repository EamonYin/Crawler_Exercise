package com.crawler.crawler_exercise.utils.redisMQ.spi_type;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class SPI_Producer {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void send(String streamKey ,String message){
        HashMap<String, String> msgMap = new HashMap<>();
        msgMap.put("msg",message);
        redisTemplate.opsForStream().add(streamKey,msgMap);
        log.info("RedisMQ 发消息:{}",message);
    }

}
