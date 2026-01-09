package com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type.impl;


import com.crawler.crawler_exercise.utils.redisMQ.annotain_type.RedisStreamListener;
import com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type.DelayRedisStreamListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DelayListenerImpl {

    @DelayRedisStreamListener(streamKey = "delay:mq:redis:order")
    public void OrderListener(String msg) {
        log.info("订单业务:{}", msg);
    }

    @DelayRedisStreamListener(streamKey = "delay:mq:redis:pay")
    public void PayListener(String msg){
        log.info("支付业务:{}", msg);
    }

}
