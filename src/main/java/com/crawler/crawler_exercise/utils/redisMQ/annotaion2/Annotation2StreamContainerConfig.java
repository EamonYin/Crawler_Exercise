package com.crawler.crawler_exercise.utils.redisMQ.annotaion2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;

/**
 * 注解2模式：方法级监听注册
 */
@Configuration
public class Annotation2StreamContainerConfig {

    private static final Logger log = LoggerFactory.getLogger(Annotation2StreamContainerConfig.class);

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> annotation2StreamContainer(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisTemplate stringRedisTemplate,
            ApplicationContext applicationContext) {

        // 1) 监听容器的基础配置（这里只设置轮询超时）
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        // 2) 创建监听容器
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // 3) 扫描所有 Bean，找出带 @RedisStreamListener 的方法
        Map<String, Object> beans = applicationContext.getBeansOfType(Object.class);
        int count = 0;
        for (Object bean : beans.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            for (Method method : targetClass.getMethods()) {
                RedisStreamListener subscription = method.getAnnotation(RedisStreamListener.class);
                if (subscription == null) {
                    continue;
                }
                register(container, stringRedisTemplate, bean, method, subscription);
                count++;
            }
        }

        container.start();
        log.info("注解2模式的 Stream 监听容器已启动，监听方法数量={}", count);
        return container;
    }

    private void register(StreamMessageListenerContainer<String, MapRecord<String, String, String>> container,
                          StringRedisTemplate stringRedisTemplate,
                          Object bean,
                          Method method,
                          RedisStreamListener subscription) {
        // 4) 确保消费者组存在（不存在就尝试创建）
        ensureGroupExists(stringRedisTemplate, subscription.streamKey(), subscription.group());
        // 5) 注册监听：收到消息就调用业务方法，必要时自动 ACK
        container.receive(
                Consumer.from(subscription.group(), subscription.consumer()),
                StreamOffset.create(subscription.streamKey(), ReadOffset.lastConsumed()),
                message -> {
                    invoke(bean, method, message);
                    if (subscription.autoAck()) {
                        // ACK：从 pending 列表移除，表示已消费
                        stringRedisTemplate.opsForStream().acknowledge(
                                subscription.streamKey(),
                                subscription.group(),
                                message.getId()
                        );
                    }
                }
        );
    }

    private void invoke(Object bean, Method method, MapRecord<String, String, String> message) {
        try {
            // 方法只允许一个参数：MapRecord 或 Map
            if (method.getParameterCount() != 1) {
                throw new IllegalStateException("Method must have one param: " + method);
            }
            Class<?> paramType = method.getParameterTypes()[0];
            if (MapRecord.class.isAssignableFrom(paramType)) {
                // 需要完整消息对象
                method.invoke(bean, message);
                return;
            }
            if (Map.class.isAssignableFrom(paramType)) {
                // 只需要 payload
                method.invoke(bean, message.getValue());
                return;
            }
            throw new IllegalStateException("Param must be MapRecord or Map: " + method);
        } catch (Exception ex) {
            throw new IllegalStateException("RedisStreamListener invoke failed: " + method, ex);
        }
    }

    private void ensureGroupExists(StringRedisTemplate stringRedisTemplate, String streamKey, String group) {
        try {
            stringRedisTemplate.opsForStream().createGroup(
                    streamKey,
                    ReadOffset.from("0"),
                    group
            );
        } catch (Exception ex) {
            log.debug("Stream 消费组已存在或无法创建：streamKey={}, group={}", streamKey, group);
        }
    }
}
