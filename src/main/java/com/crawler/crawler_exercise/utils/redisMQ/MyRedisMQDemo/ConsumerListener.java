package com.crawler.crawler_exercise.utils.redisMQ.MyRedisMQDemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsumerListener implements StreamListener<String, MapRecord<String,String,String>> {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String msg = message.getValue().get("msg");
        log.info("[消费者]监听到的消息:{}",msg);
        /**
         * 【XACK 确认消息命令】
         *
         * 下面代码等价于
         * XACK demo-stream demo-group 1699999999999-0
         */
        redisTemplate.opsForStream().acknowledge(RedisMqConst.STREAM_KEY,RedisMqConst.GROUP,message.getId());
    }
}
