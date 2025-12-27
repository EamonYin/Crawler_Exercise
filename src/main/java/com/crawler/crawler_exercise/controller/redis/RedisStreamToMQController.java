package com.crawler.crawler_exercise.controller.redis;

import com.crawler.crawler_exercise.utils.redisMQ.oldDemo.RedisMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/redis/stream")
public class RedisStreamToMQController {

    @Autowired
    private RedisMQSender redisMQProducer;


    /**
     * 效果说明：
     * /redis/stream/send 发送一条消息到 demo:stream
     * 两个监听器分别在各自消费组里消费同一条消息并打印中文日志（模拟两条业务逻辑）
     * 你可以下一步：
     *
     * 启动服务后请求 GET /redis/stream/send，看控制台日志是否出现两条模拟业务日志。
     * 如果要区分两种业务类型，我可以改成两个不同 stream 或在消息里加业务字段。
     */
    @GetMapping("/send")
    public String sendMessage() {
        Map<String, String> message = new HashMap<>();
        message.put("orderId", "123");
        message.put("event", "CREATE");
        message.put("time", String.valueOf(System.currentTimeMillis()));
        message.put("source", "demo");

        redisMQProducer.send("demo:stream", message);
        return "发送成功";
    }

}
