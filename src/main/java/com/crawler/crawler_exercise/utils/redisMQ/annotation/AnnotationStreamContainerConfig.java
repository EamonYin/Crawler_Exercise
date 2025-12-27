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
// 只在“注解模式”开启时生效(applciation.yml 中配置了 redis.mq.mode=annotation,RedisMqMode包含了所有redis做mq的模式)
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
        // 1) 扫描所有标了 @RedisStreamSubscription 的 Bean
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RedisStreamSubscription.class);
        for (Object bean : beans.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            RedisStreamSubscription subscription = targetClass.getAnnotation(RedisStreamSubscription.class);
            // 2) 要求必须实现 StreamListener
            if (!(bean instanceof StreamListener)) {
                throw new IllegalStateException("Bean must implement StreamListener: " + targetClass.getName());
            }

            // 3) 启动前确保消费组存在
            ensureGroupExists(stringRedisTemplate, subscription.streamKey(), subscription.group());

            @SuppressWarnings("unchecked")
            StreamListener<String, MapRecord<String, String, String>> listener =
                    (StreamListener<String, MapRecord<String, String, String>>) bean;

            // 4) 注册监听：消费后执行业务，再决定是否 ACK
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

        // 5) 手动启动容器(container)
        container.start();
        log.info("注解模式的 Stream 监听容器已启动，监听器数量={}", beans.size());

        return container;
    }

    // 尝试创建消费组，失败就吞掉（通常是已存在/流未创建）
    private void ensureGroupExists(StringRedisTemplate stringRedisTemplate, String streamKey, String group) {
        try {
            stringRedisTemplate.opsForStream().createGroup(
                    streamKey,
                    ReadOffset.from("0"),
                    group
            );
        } catch (Exception ex) {
            // 正常情况：组已存在或流还未创建
            log.debug("Stream 消费组已存在或无法创建：streamKey={}, group={}", streamKey, group);
        }
    }
}
