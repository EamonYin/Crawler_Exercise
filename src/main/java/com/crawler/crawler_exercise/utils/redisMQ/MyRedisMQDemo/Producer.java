package com.crawler.crawler_exercise.utils.redisMQ.MyRedisMQDemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class Producer {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void send(String message){
        HashMap<String, String> msgMap = new HashMap<>();
        msgMap.put("msg",message);
        /**
         * 【XADD 发送消息命令】
         *
         * 下面代码等价于
         * XADD demo-stream * msg "hello redis mq"
         *
         * 往 demo-stream 里
         * 👉 加一条消息
         * 👉 * 表示 ID 让 Redis 自动生成
         * 👉 内容是 msg=hello redis mq
         *
         * Redis 生成的真实数据类似：
         * 1699999999999-0
         * └── msg = "hello redis mq"
         */
        redisTemplate.opsForStream().add(RedisMqConst.STREAM_KEY,msgMap);
        log.info("RedisMQ 发消息:{}",message);
    }

}
