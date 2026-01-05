package com.crawler.crawler_exercise.utils.redisMQ.easy_type;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ContainerConfig {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ConsumerListener consumerListener;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    /**
     * @PostConstruct注解解析
     * Spring 官方推荐的“安全初始化点”
     * Spring 启动完成、Bean 注入后、执行这个注解下的方法（自动启动 Redis Stream 的监听，让 MQ 从一开始就“活着”）
     * 可以确保 @Autowired 注入完成，即redisTemplate、consumer不为空
    **/
    @PostConstruct
    public void init(){
        // 1. 创建消费者组（如果已存在会报错，直接忽略）
        try{
            /**
             * 【XGROUP CREATE 创建消费者组命令】
             *
             * 下面的代码等价于
             * XGROUP CREATE demo-stream demo-group $
             *
             * $ 的意思是：
             * 历史消息不要了，只看以后新来的
             */
            redisTemplate.opsForStream().createGroup(RedisMqConst.STREAM_KEY, RedisMqConst.GROUP);
        }catch (Exception e){}
        // 2. 启动监听容器
        container = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer
                        .StreamMessageListenerContainerOptions
                        .builder()
                        .build());

        /**
         * 【XREADGROUP 监听 / 读取消息命令】
         *
         * 下面的代码等价于
         * XREADGROUP
         *   GROUP <group> <consumer>
         *   COUNT <n>
         *   BLOCK <ms>
         *   STREAMS <stream-key> <id>
         * XREADGROUP GROUP demo-group consumer-1 COUNT 1 BLOCK 0 STREAMS demo-stream >
         * 结尾“>”含义：只读取这个【消费者组】从未处理过的新消息
         */
        container.receive(Consumer.from(RedisMqConst.GROUP,RedisMqConst.CONSUMER),
                StreamOffset.create(RedisMqConst.STREAM_KEY, ReadOffset.lastConsumed()),
                consumerListener);

        container.start();
    }

    /**
     * 2026-01-05: 加了也没用！
     * @PostConstruct 是SpringBoot启动时创建容器
     * @PreDestroy 是SpringBoot结束关闭容器
     */
    @PreDestroy
    public void shutdown() {
        if (container != null) {
            container.stop();
        }
    }
}
