package com.crawler.crawler_exercise.utils.redisMQ.annotation;

import com.crawler.crawler_exercise.utils.redisMQ.RedisMqMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.Map;

/**
 * 注解模式的容器配置与绑定入口
 */
@Configuration
@ConditionalOnProperty(name = RedisMqMode.PROPERTY_NAME, havingValue = RedisMqMode.MODE_ANNOTATION)
public class AnnotationStreamContainerConfig {

    private static final Logger log = LoggerFactory.getLogger(AnnotationStreamContainerConfig.class);

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> annotationStreamContainer(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisTemplate stringRedisTemplate,
            ApplicationContext applicationContext) {

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // 扫描所有带注解的监听器
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RedisStreamSubscription.class);
        for (Object bean : beans.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            RedisStreamSubscription subscription = targetClass.getAnnotation(RedisStreamSubscription.class);
            if (!(bean instanceof StreamListener)) {
                throw new IllegalStateException("Bean must implement StreamListener: " + targetClass.getName());
            }

            // 启动前确保消费者组存在
            ensureGroupExists(stringRedisTemplate, subscription.streamKey(), subscription.group());

            @SuppressWarnings("unchecked")
            StreamListener<String, MapRecord<String, String, String>> listener =
                    (StreamListener<String, MapRecord<String, String, String>>) bean;

            container.receive(
                    Consumer.from(subscription.group(), subscription.consumer()),
                    StreamOffset.create(subscription.streamKey(), ReadOffset.lastConsumed()),
                    message -> {
                        // 先执行业务逻辑，再决定是否 ACK
                        listener.onMessage(message);
                        if (subscription.autoAck()) {
                            stringRedisTemplate.opsForStream().acknowledge(
                                    subscription.streamKey(),
                                    subscription.group(),
                                    message.getId()
                            );
                        }
                    }
            );
        }

        // 手动启动 container
        container.start();
        log.info("Annotation stream container started with listeners={}", beans.size());

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
