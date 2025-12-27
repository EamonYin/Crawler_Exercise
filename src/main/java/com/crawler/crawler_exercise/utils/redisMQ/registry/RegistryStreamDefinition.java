package com.crawler.crawler_exercise.utils.redisMQ.registry;

/**
 * 注册表模式的 Stream 基本配置
 */
public final class RegistryStreamDefinition {

    private final String streamKey;
    private final String group;
    private final String consumer;
    private final boolean autoAck;

    public RegistryStreamDefinition(String streamKey, String group, String consumer, boolean autoAck) {
        this.streamKey = streamKey;
        this.group = group;
        this.consumer = consumer;
        this.autoAck = autoAck;
    }

    public static RegistryStreamDefinition of(String streamKey, String group, String consumer) {
        // 默认开启自动 ACK
        return new RegistryStreamDefinition(streamKey, group, consumer, true);
    }

    public String getStreamKey() {
        return streamKey;
    }

    public String getGroup() {
        return group;
    }

    public String getConsumer() {
        return consumer;
    }

    public boolean isAutoAck() {
        return autoAck;
    }
}
