package com.crawler.crawler_exercise.utils.redisMQ.zset_delay_annotain_type;

import com.alibaba.dashscope.utils.JsonUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@ConditionalOnProperty(name = "redis.mq.mode", havingValue = "zsetDelay") //用于切换 annotain / spi / easy / delay 三种redis实现
public class RedisStreamDelayListenerRegistrar implements SmartInitializingSingleton, ApplicationContextAware {

    /** 用于查找带有自定义注解 */
    private ApplicationContext applicationContext;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final Map<String, List<DelayListenerInvoker>> listeners = new ConcurrentHashMap<>();

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);

        beans.values().forEach(bean -> {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                RedisStreamDelayListener listener =
                        AnnotationUtils.findAnnotation(method, RedisStreamDelayListener.class);

                if (listener != null) {
                    registerListener(bean, method, listener);
                }
            }
        });
    }

    /**
     * 【为什么要“移除 StreamMessageListenerContainer 的 receive 注册；改为维护 streamKey -> listeners 的映射”？】
     * 因为 StreamMessageListenerContainer.receive(...) 绑定的是 Redis Stream 的消费模型（XREADGROUP/pending/ack），它负责“阻塞拉取 + 分发”。
     * 延时队列用 ZSET + 时间分数，没有“推”或“阻塞读”，只能靠定时轮询到期消息。所以：
     *
     * 不能再让容器去读 Stream（它只认识 Stream 协议）
     * 需要自己维护 streamKey -> listeners，轮询每个 streamKey 的 ZSET，把到期消息分发给对应监听器
     * 简单讲：容器是“Stream 的拉取器”，延时队列是**“ZSET 的定时扫描器”**，机制不同，所以要换成映射+轮询。
     */
    private void registerListener(Object bean, Method method, RedisStreamDelayListener listener) {
        method.setAccessible(true);
        listeners.computeIfAbsent(listener.streamKey(), key -> new CopyOnWriteArrayList<>())
                .add(new DelayListenerInvoker(bean, method, listener));
    }

    @Scheduled(fixedDelayString = "${redis.mq.delay.poll-interval-ms:1000}")
    public void pollDelayQueues() {
        if (listeners.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        for (Map.Entry<String, List<DelayListenerInvoker>> entry : listeners.entrySet()) {
            String queueKey = entry.getKey();
            Set<String> messages = redisTemplate.opsForZSet().rangeByScore(queueKey, 0, now);
            if (messages == null || messages.isEmpty()) {
                continue;
            }

            boolean autoAck = entry.getValue().stream().allMatch(invoker -> invoker.listener.autoAck());
            for (String message : messages) {
                for (DelayListenerInvoker invoker : entry.getValue()) {
                    invokeListener(invoker, message);
                }
            }
            if (autoAck) {
                redisTemplate.opsForZSet().remove(queueKey, messages.toArray());
            }
        }
    }

    private void invokeListener(DelayListenerInvoker invoker, String message) {
        try {
            Class<?> paramType = invoker.method.getParameterTypes()[0];
            Object arg = (paramType == String.class) ? message : JsonUtils.fromJson(message, paramType);
            invoker.method.invoke(invoker.bean, arg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DelayListenerInvoker {
        private final Object bean;
        private final Method method;
        private final RedisStreamDelayListener listener;

        private DelayListenerInvoker(Object bean, Method method, RedisStreamDelayListener listener) {
            this.bean = bean;
            this.method = method;
            this.listener = listener;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
