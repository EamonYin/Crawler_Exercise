package com.crawler.crawler_exercise.utils.redisMQ.annotain_type;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisStreamListener {

    // Stream key
    String streamKey();

    // 消费组
    String group() default "default-group";

    // 消费者名
    String consumer() default "";

    // 是否自动 ack
    boolean autoAck() default true;
}
