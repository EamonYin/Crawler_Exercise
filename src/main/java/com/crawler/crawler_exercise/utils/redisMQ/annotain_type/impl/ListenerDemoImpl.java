package com.crawler.crawler_exercise.utils.redisMQ.annotain_type.impl;


import com.crawler.crawler_exercise.utils.redisMQ.annotain_type.RedisStreamListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ListenerDemoImpl {

    @RedisStreamListener(streamKey = "demo:mq:redis:order")
    public void OrderListener(String msg) {
        log.info("订单业务:{}", msg);
    }

    @RedisStreamListener(streamKey = "demo:mq:redis:pay")
    public void PayListener(String msg){
        log.info("支付业务:{}", msg);
    }

}
