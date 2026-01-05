package com.crawler.crawler_exercise.utils.redisMQ.annotain_type;

import com.alibaba.dashscope.utils.JsonUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

@Component
public class RedisStreamListenerRegistrar implements SmartInitializingSingleton, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);

        beans.values().forEach(bean -> {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                com.crawler.crawler_exercise.utils.redisMQ.annotain_type.RedisStreamListener listener =
                        AnnotationUtils.findAnnotation(method, com.crawler.crawler_exercise.utils.redisMQ.annotain_type.RedisStreamListener.class);

                if (listener != null) {
                    registerListener(bean, method, listener);
                }
            }
        });
    }

    private void registerListener(Object bean, Method method, com.crawler.crawler_exercise.utils.redisMQ.annotain_type.RedisStreamListener listener) {

        String streamKey = listener.streamKey();
        String group = listener.group();
        String consumer = listener.consumer().isEmpty()
                ? UUID.randomUUID().toString()
                : listener.consumer();

        // 确保 group 存在
        try {
            redisTemplate.opsForStream()
                    .createGroup(streamKey, group);
        } catch (Exception ignore) {
        }

        container.receive(
                Consumer.from(group, consumer),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                message -> invokeString(bean, method, message, listener)
        );
    }

    /**
     * 在其中将生产者的String反序列化为对象
     *
     * 【问题】
     * 消息一定是合法 JSON
     * JSON 一定能映射到目标 Java 类
     * 字段类型完全匹配（时间、枚举、Long / String）
     * 消费者参数类型不会改
     * 不存在泛型 / 版本不兼容
     *
     * 👉 任何一个不成立，异常都会出现在“框架层”
     */
    private void invokeMethod(
            Object bean,
            Method method,
            MapRecord<String, String, String> record,
            com.crawler.crawler_exercise.utils.redisMQ.annotain_type.RedisStreamListener listener) {

        try {
            //等价于：json = {"orderId":1001,"userId":2002}
            String json = record.getValue().get("data");

            //等价于：paramType = OrderCreateMessage.class
            Class<?> paramType = method.getParameterTypes()[0];

            //等价于：arg = new OrderCreateMessage(1001, 2002)
            Object arg;
            if (paramType == String.class) {
                arg = json;
            } else {
                arg = JsonUtils.fromJson(json, paramType);
            }

            // 通过反射调用业务方法
            // 等价于：((OrderConsumer) bean).onOrderCreate((OrderCreateMessage) arg);
            method.invoke(bean, arg);

            if (listener.autoAck()) {
                redisTemplate.opsForStream().acknowledge(
                        record.getStream(),
                        listener.group(),
                        record.getId()
                );
            }
        } catch (Exception e) {
            // 记录日志，后续可支持重试 / DLQ
            e.printStackTrace();
        }
    }

    /**
     * 相对于invokeMethod，
     * 只是原样返回生产者的消息String，反序列化交给对应的消费者
     */
    private void invokeString(
            Object bean,
            Method method,
            MapRecord<String, String, String> record,
            com.crawler.crawler_exercise.utils.redisMQ.annotain_type.RedisStreamListener listener) {

        try {
            String message = record.getValue().get("data");

            method.setAccessible(true);
            method.invoke(bean, message);

            if (listener.autoAck()) {
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