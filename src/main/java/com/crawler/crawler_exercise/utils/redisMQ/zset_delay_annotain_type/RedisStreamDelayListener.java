package com.crawler.crawler_exercise.utils.redisMQ.zset_delay_annotain_type;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisStreamDelayListener {

    // ZSet key
    String streamKey();

    // 兼容字段，ZSet 方式不使用
    String group() default "default-group";

    // 兼容字段，ZSet 方式不使用
    String consumer() default "";

    // 是否自动 ack（为 false 时不移除消息）
    boolean autoAck() default true;
}
