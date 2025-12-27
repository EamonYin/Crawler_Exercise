package com.crawler.crawler_exercise.utils.redisMQ.oldDemo;

import com.crawler.crawler_exercise.utils.redisMQ.RedisMqMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = RedisMqMode.PROPERTY_NAME, havingValue = RedisMqMode.MODE_OLD_DEMO, matchIfMissing = true)
public class SimpleRedisStreamContainerConfig {
    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
    streamContainer(RedisConnectionFactory redisConnectionFactory,
                    RedisMQConsumer orderStreamListener) {

        // 1️⃣ 创建最简单的配置（几乎不配）
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1)) // 阻塞 1 秒
                        .build();

        // 2️⃣ 创建 Container
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // 3️⃣ 绑定 Stream + Group + Consumer
        container.receive(
                Consumer.from("order-group", "consumer-1"),
                StreamOffset.create("order:stream", ReadOffset.lastConsumed()),
                orderStreamListener
        );

        // 4️⃣ 启动监听（最关键）
        container.start();

        System.out.println("🚀 Redis Stream Container started");

        return container;
    }
}
