package com.crawler.crawler_exercise.utils.redisMQ.Redisson.listener;

import com.crawler.crawler_exercise.utils.redisMQ.Redisson.RedisDelayedQueueListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单支付过期监听
 */
@Component
@Slf4j
public class OrderListener implements RedisDelayedQueueListener {
    @Override
    public void invoke(Object o) {
        System.out.println("订单支付过期监听");
    }
}
