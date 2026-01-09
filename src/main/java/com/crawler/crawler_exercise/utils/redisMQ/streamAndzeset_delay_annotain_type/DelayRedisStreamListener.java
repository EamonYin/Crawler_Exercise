package com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DelayRedisStreamListener {

    // Stream key
    String streamKey();

    // 消费组
    String group() default "default-group";

    // 消费者名
    String consumer() default "";

    // 是否自动 ack
    boolean autoAck() default true;
}
