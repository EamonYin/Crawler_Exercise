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

    @GetMapping("/send")
    public String sendMessage() {
        Map<String, String> message = new HashMap<>();
        message.put("orderId", "123");
        message.put("event", "CREATE");
        message.put("time", String.valueOf(System.currentTimeMillis()));

        redisMQProducer.send("order:stream", message);
        return "发送成功";
    }

}
