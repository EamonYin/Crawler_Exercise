package com.crawler.crawler_exercise.utils.redisMQ.registry;

/**
 * 注册表模式的配置与处理器绑定关系
 */
public final class RegistryStreamBinding {

    // 配置与处理器的绑定关系
    private final RegistryStreamDefinition definition;
    private final RegistryStreamHandler handler;

    public RegistryStreamBinding(RegistryStreamDefinition definition, RegistryStreamHandler handler) {
        this.definition = definition;
        this.handler = handler;
    }

    public RegistryStreamDefinition getDefinition() {
        return definition;
    }

    public RegistryStreamHandler getHandler() {
        return handler;
    }
}
