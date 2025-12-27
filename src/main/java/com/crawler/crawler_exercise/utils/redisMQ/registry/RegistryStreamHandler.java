package com.crawler.crawler_exercise.utils.redisMQ.registry;

import org.springframework.data.redis.connection.stream.MapRecord;

/**
 * 注册表模式的业务处理接口
 */
public interface RegistryStreamHandler {

    // 处理器绑定的 stream key
    String streamKey();

    // 业务处理逻辑
    void handle(MapRecord<String, String, String> record);
}
