package com.crawler.crawler_exercise.utils.redisMQ.oldDemo;

import java.util.Map;

/**
 * 通用 MQ 发送接口，屏蔽不同实现方案的差异
 */
public interface RedisMQSender {

    // 发送消息到指定 stream
    void send(String streamKey, Map<String, String> message);
}
