package com.crawler.crawler_exercise.utils.redisMQ.Redisson;


import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * Redis 延时队列生产者工具类
 */
@Component
@Slf4j
public class RedisDelayedQueueProducerUtils {


    @Autowired
    private RedissonClient redissonClient;

    /**
     * 添加对象进延时队列
     * @param putInData 添加数据
     * @param delay     延时时间
     * @param timeUnit  时间单位
     * @param queueName 队列名称
     * @param <T>
     */
    public <T> void addQueue(T putInData, long delay, TimeUnit timeUnit, String queueName){
        log.info("添加延迟队列,监听名称:{},时间:{},时间单位:{},内容:{}" , queueName, delay, timeUnit,putInData);
        RBlockingQueue<T> blockingFairQueue = redissonClient.getBlockingQueue(queueName);
        RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
        delayedQueue.offer(putInData, delay, timeUnit);
    }
}
