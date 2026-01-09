package com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.util.ErrorHandler;

import java.time.Duration;

@Configuration
// 与 annotain_type 的配置类似，但只在 streamZsetDelay 模式启用。
@ConditionalOnProperty(name = "redis.mq.mode", havingValue = "streamZsetDelay") //用于切换 annotain / spi / easy 三种redis实现
public class DelayAnnotainContainerConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Primary
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> annotainStreamContainer(
            RedisConnectionFactory redisConnectionFactory,
            ErrorHandler errorHandler) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .errorHandler(errorHandler) //吞掉停止项目时的报错（详见com.crawler.crawler_exercise.utils.redisMQ.easy_type.ContainerConfig讲解）
                        .build();

        return StreamMessageListenerContainer.create(redisConnectionFactory, options);
    }
}
