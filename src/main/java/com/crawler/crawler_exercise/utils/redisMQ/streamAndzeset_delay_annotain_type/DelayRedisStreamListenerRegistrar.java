package com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "redis.mq.mode", havingValue = "streamZsetDelay") //用于切换 annotain / spi / easy 三种redis实现
public class DelayRedisStreamListenerRegistrar implements SmartInitializingSingleton, ApplicationContextAware {

    /** 用于查找带有自定义注解 */
    private ApplicationContext applicationContext;

    /**
     * Redis Stream 消息监听容器
     * 职责：
     *   - 负责 XREADGROUP 拉取消息
     *   - 将消息分发给已注册的监听器
     */
    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final Set<String> streamKeys = ConcurrentHashMap.newKeySet();

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);

        beans.values().forEach(bean -> {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                 DelayRedisStreamListener listener =
                        AnnotationUtils.findAnnotation(method,  DelayRedisStreamListener.class);

                if (listener != null) {
                    // 注册为 Redis Stream 消费者
                    registerListener(bean, method, listener);
                }
            }
        });
    }

    private void registerListener(Object bean, Method method,  DelayRedisStreamListener listener) {

        // 读取注解配置
        String streamKey = listener.streamKey();
        String group = listener.group();
        streamKeys.add(streamKey);
        // 生成 Consumer 名称
        String consumer = listener.consumer().isEmpty()
                ? UUID.randomUUID().toString()
                : listener.consumer();

        // 确保 group 存在
        ensureGroup(streamKey, group);

        container.receive(
                Consumer.from(group, consumer), // 指定消费组 & 消费者
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()), // 从未 ack 的位置开始
                message -> invokeString(bean, method, message, listener) // 消息回调
        );
    }

    Set<String> getStreamKeys() {
        return streamKeys;
    }

    private void ensureGroup(String streamKey, String group) {
        try {
            redisTemplate.execute((RedisCallback<String>) connection -> {
                byte[] rawKey = redisTemplate.getStringSerializer().serialize(streamKey);
                if (rawKey == null) {
                    return null;
                }
                return connection.streamCommands().xGroupCreate(rawKey, group, ReadOffset.latest(), true);
            });
        } catch (Exception ignore) {
        }
    }

    /**
     * 相对于invokeMethod，
     * 只是原样返回生产者的消息String，反序列化交给对应的消费者
     */
    private void invokeString(
            Object bean, // Spring 容器里的 真实 Bean 实例
            Method method, // Spring 容器里的 真实 Bean 实例
            MapRecord<String, String, String> record, // Redis Stream 的一条消息
            DelayRedisStreamListener listener // 注解实例
            ) {

        try {
            // 取消息体
            String message = record.getValue().get("data");
            method.setAccessible(true);

            //反射调用自定义注解所对应的业务方法
            method.invoke(bean, message);

            if (listener.autoAck()) {
                // 等价于[XACK]命令
                redisTemplate.opsForStream().acknowledge(
                        record.getStream(),
                        listener.group(),
                        record.getId()
                );
            }
        } catch (Exception e) {
            // 这里只剩：反射失败 / 业务异常
            e.printStackTrace();
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
