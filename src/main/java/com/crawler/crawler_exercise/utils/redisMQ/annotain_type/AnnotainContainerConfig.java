package com.crawler.crawler_exercise.utils.redisMQ.annotain_type;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

@Configuration
public class AnnotainContainerConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Primary
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> annotainStreamContainer(
            RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        return StreamMessageListenerContainer.create(redisConnectionFactory, options);
    }
}
