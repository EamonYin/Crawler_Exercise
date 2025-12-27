package com.crawler.crawler_exercise.utils.redisMQ.registry;

import com.crawler.crawler_exercise.utils.redisMQ.RedisMqMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.List;

/**
 * 注册表模式的容器配置与绑定入口
 */
@Configuration
@ConditionalOnProperty(name = RedisMqMode.PROPERTY_NAME, havingValue = RedisMqMode.MODE_REGISTRY)
public class RegistryStreamContainerConfig {

    private static final Logger log = LoggerFactory.getLogger(RegistryStreamContainerConfig.class);

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> registryStreamContainer(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisTemplate stringRedisTemplate,
            RegistryStreamRegistry registry) {

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        List<RegistryStreamBinding> bindings = registry.getBindings();
        for (RegistryStreamBinding binding : bindings) {
            RegistryStreamDefinition definition = binding.getDefinition();
            // 启动前确保消费者组存在
            ensureGroupExists(stringRedisTemplate, definition.getStreamKey(), definition.getGroup());

            container.receive(
                    Consumer.from(definition.getGroup(), definition.getConsumer()),
                    StreamOffset.create(definition.getStreamKey(), ReadOffset.lastConsumed()),
                    message -> {
                        // 先执行业务逻辑，再决定是否 ACK
                        binding.getHandler().handle(message);
                        if (definition.isAutoAck()) {
                            stringRedisTemplate.opsForStream().acknowledge(
                                    definition.getStreamKey(),
                                    definition.getGroup(),
                                    message.getId()
                            );
                        }
                    }
            );
        }

        // 手动启动 container
        container.start();
        log.info("Registry stream container started with bindings={}", bindings.size());

        return container;
    }

    private void ensureGroupExists(StringRedisTemplate stringRedisTemplate, String streamKey, String group) {
        try {
            stringRedisTemplate.opsForStream().createGroup(
                    streamKey,
                    ReadOffset.from("0"),
                    group
            );
        } catch (Exception ex) {
            // 正常情况：组已存在或流还未创建
            log.debug("Stream group exists or cannot be created: streamKey={}, group={}", streamKey, group);
        }
    }
}
