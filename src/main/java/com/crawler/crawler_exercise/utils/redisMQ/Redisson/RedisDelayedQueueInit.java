package com.crawler.crawler_exercise.utils.redisMQ.Redisson;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * redis 延时队列初始化
 */
@Component
@Slf4j
public class RedisDelayedQueueInit implements ApplicationContextAware {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, RedisDelayedQueueListener> beanMap = applicationContext.getBeansOfType(RedisDelayedQueueListener.class);
        for (RedisDelayedQueueListener listener : beanMap.values()) {
            String queueName = listener.getClass().getName();
            startThread(queueName, listener);
        }
    }

    /**
     * 启动线程获取队列
     * @param queueName 队列名称
     * @param redisDelayedQueueListener 任务回调监听
     */
    private <T> void startThread(String queueName, RedisDelayedQueueListener redisDelayedQueueListener) {
        RBlockingQueue<T> blockingFairQueue = redissonClient.getBlockingQueue(queueName);
        // Start delayed queue scheduler so expired items are moved into blocking queue.
        RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);

        Thread thread = new Thread(() -> {
            log.info("启动监听队列线程" + queueName);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    T t = blockingFairQueue.take();
                    log.info("监听队列线程{},获取到值:{}", queueName, JSON.toJSONString(t));
                    redisDelayedQueueListener.invoke(t);
                } catch (Exception e) {
                    log.info("监听队列线程错误,", e);
                }
            }
        });

        thread.setName(queueName);
        thread.start();
    }
}
