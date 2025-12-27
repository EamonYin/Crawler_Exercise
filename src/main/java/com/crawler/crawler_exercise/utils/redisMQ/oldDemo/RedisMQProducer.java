package com.crawler.crawler_exercise.utils.redisMQ.oldDemo;

import com.crawler.crawler_exercise.utils.redisMQ.RedisMqMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 旧示例模式的发送实现
 */
@Component
@ConditionalOnProperty(name = RedisMqMode.PROPERTY_NAME, havingValue = RedisMqMode.MODE_OLD_DEMO, matchIfMissing = true)
public class RedisMQProducer implements RedisMQSender {

    private static final String STREAM_KEY = "order:stream";
    private static final String GROUP_NAME = "order-group";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送一条最简单的消息
     */
    public void sendMessage(String orderId) {

        // 1. 构造消息体（KV 结构）
        Map<String, String> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("event", "CREATE");
        message.put("time", String.valueOf(System.currentTimeMillis()));

        // 2. 写入 Redis Stream（XADD）
        // XADD order:stream * orderId 123 event CREATE time 1700000000000
        stringRedisTemplate.opsForStream().add(STREAM_KEY, message);

        System.out.println("✅ 消息已发送到 Redis Stream，orderId=" + orderId);
    }

    @Override
    public void send(String streamKey, Map<String, String> message) {
        // 通用发送入口，支持自定义 streamKey
        stringRedisTemplate.opsForStream().add(streamKey, message);
    }

}
