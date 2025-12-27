package com.crawler.crawler_exercise.utils.redisMQ.oldDemo;

import com.crawler.crawler_exercise.utils.redisMQ.RedisMqMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 消费者组
 * StreamListener 是泛型接口，<K, V> 表示流的 key 类型和消息记录类型。
 * 这里 K=String（stream key 是字符串），V=MapRecord<String, String, String>（消息记录是 map 结构的 record）。
 *
 * MapRecord<String, String, String> 的三个泛型参数分别对应：
 * 第一个 String：stream key 的类型（例如 "order:stream"）
 * 第二个 String：消息字段名（hash key）类型
 * 第三个 String：消息字段值（hash value）类型
 */
@Component
@ConditionalOnProperty(name = RedisMqMode.PROPERTY_NAME, havingValue = RedisMqMode.MODE_OLD_DEMO, matchIfMissing = true)
public class RedisMQConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private static final String STREAM_KEY = "order:stream";
    private static final String GROUP_NAME = "order-group";
    private static final String CONSUMER_NAME = "consumer-1";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        try {
            // 1. 创建消费者组（如果 Stream 不存在，会自动创建 Stream）
            stringRedisTemplate.opsForStream().createGroup(
                    STREAM_KEY,
                    ReadOffset.from("0"),
                    GROUP_NAME
            );

            System.out.println("✅ Redis Stream + Consumer Group 创建成功");

        } catch (Exception e) {
            // 2. 说明 Group 已存在（最常见的正常情况）
            System.out.println("⚠️ Redis Stream 或 Consumer Group 已存在，无需重复创建");
        }
    }

    /**
     * 读取一条消息并 ACK
     */
    public void consumeOnce() {

        // 1. 从消费者组中读取消息
        // XREADGROUP GROUP order-group consumer-1 COUNT 1 STREAMS order:stream >
        List<MapRecord<String, String, String>> records =
                stringRedisTemplate.<String, String>opsForStream().read(
                        Consumer.from(GROUP_NAME, CONSUMER_NAME),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                );

        // 2. 没有消息，直接返回
        if (records == null || records.isEmpty()) {
            System.out.println("📭 暂无消息");
            return;
        }

        // 3. 处理消息
        for (MapRecord<String, String, String> record : records) {
            handleMessage(record);
        }
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        handleMessage(message);
    }

    private void handleMessage(MapRecord<String, String, String> record) {

        System.out.println("📩 收到消息：" + record.getId());
        System.out.println("📦 内容：" + record.getValue());

        // 👉 业务处理
        // process(record.getValue());

        // 👉 ACK
        stringRedisTemplate.opsForStream().acknowledge(
                STREAM_KEY,
                GROUP_NAME,
                record.getId()
        );

        System.out.println("✅ 消息已 ACK：" + record.getId());
    }
}
