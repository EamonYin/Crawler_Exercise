package com.crawler.crawler_exercise.utils.redisMQ.annotation.demo;

import com.crawler.crawler_exercise.utils.redisMQ.annotation.RedisStreamSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 注解模式 Demo：模拟 Redis 数据更新业务
 */
@Component
@RedisStreamSubscription(
        streamKey = "demo:stream",
        group = "redis-update-group",
        consumer = "redis-update-consumer",
        autoAck = true
)
public class RedisDataUpdateStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    private static final Logger log = LoggerFactory.getLogger(RedisDataUpdateStreamListener.class);

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> payload = message.getValue();
        log.info("模拟更新 Redis 数据：messageId={}, data={}", message.getId(), payload);
    }
}
