package com.crawler.crawler_exercise.utils.redisMQ;

/**
 * Redis MQ 模式常量，统一控制启用的实现方案
 */
public final class RedisMqMode {

    // 通过配置项切换不同 MQ 实现
    public static final String PROPERTY_NAME = "crawler.redis.mq.mode";
    public static final String MODE_OLD_DEMO = "oldDemo";
    public static final String MODE_REGISTRY = "registry";
    public static final String MODE_ANNOTATION = "annotation";

    private RedisMqMode() {
    }
}
