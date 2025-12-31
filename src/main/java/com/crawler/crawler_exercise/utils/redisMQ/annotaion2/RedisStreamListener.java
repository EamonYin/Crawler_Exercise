package com.crawler.crawler_exercise.utils.redisMQ.annotaion2;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级 Stream 监听注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisStreamListener {

    String streamKey();

    String group();

    String consumer();

    boolean autoAck() default true;
}
