package com.crawler.crawler_exercise.utils.redisMQ.spi_type;

import com.crawler.crawler_exercise.utils.redisMQ.easy_type.RedisMqConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SPI_ConsumerListener implements StreamListener<String, MapRecord<String,String,String>> {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String msg = message.getValue().get("msg");
        log.info("[消费者]监听到的消息:{}",msg);


        redisTemplate.opsForStream().acknowledge(message.getStream(),RedisMqConst.GROUP,message.getId());
    }
}
