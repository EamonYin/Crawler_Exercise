package com.crawler.crawler_exercise.utils.redisMQ.annotation;

import com.crawler.crawler_exercise.utils.redisMQ.RedisMqMode;
import com.crawler.crawler_exercise.utils.redisMQ.oldDemo.RedisMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 注解模式的统一发送入口
 */
@Component
@ConditionalOnProperty(name = RedisMqMode.PROPERTY_NAME, havingValue = RedisMqMode.MODE_ANNOTATION)
public class AnnotationStreamProducer implements RedisMQSender {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void send(String streamKey, Map<String, String> message) {
        // 统一入口，按 streamKey 写入
        stringRedisTemplate.opsForStream().add(streamKey, message);
    }
}
