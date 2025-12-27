package com.crawler.crawler_exercise.utils.redisMQ.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解模式：声明 Stream 订阅信息
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisStreamSubscription {

    // 目标 stream key
    String streamKey();

    // 消费者组
    String group();

    // 消费者名称
    String consumer();

    // 是否由框架自动 ACK
    boolean autoAck() default true;
}
