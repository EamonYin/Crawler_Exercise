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

    /**
     * 让 StreamMessageListenerContainer 成为 Spring 管理的单例组件，其他地方可直接注入使用。
     * Spring 会在启动时执行这个方法，自动完成容器的创建和启动逻辑（你在方法里 container.start()）。
     * 结合 @Configuration 和 @ConditionalOnProperty，只在满足配置条件时才创建这个监听容器。
     * 简单说：@Bean 把“手动 new 的容器”变成“Spring 管理的容器”，便于统一配置、自动装配和生命周期控制。
     */
    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> annotationStreamContainer(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisTemplate stringRedisTemplate,
            ApplicationContext applicationContext) {

        // 1️⃣ 创建最简单的配置（几乎不配）
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1)) // Redis Stream 阻塞拉取超时（XREADGROUP BLOCK）
                        .build();

        // 2️⃣ 创建 Container
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
                    Consumer.from(subscription.group(), subscription.consumer()), // 指定消费者组 + 消费者名
                    StreamOffset.create(subscription.streamKey(), ReadOffset.lastConsumed()), // 只拉取新消息（">"）
                    message -> {
                        // 先执行业务逻辑，再决定是否 ACK
                        listener.onMessage(message);
                        if (subscription.autoAck()) {
                            // ACK 确认：从 Pending 列表移除
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
