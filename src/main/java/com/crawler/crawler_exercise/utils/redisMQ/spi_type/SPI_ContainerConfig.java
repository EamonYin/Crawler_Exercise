package com.crawler.crawler_exercise.utils.redisMQ.spi_type;

import com.crawler.crawler_exercise.utils.redisMQ.easy_type.ConsumerListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name = "redis.mq.mode", havingValue = "spi")
public class SPI_ContainerConfig {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ConsumerListener consumerListener;

    @Autowired
    private List<RedisConsumerRegistrar> registrars;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @PostConstruct
    public void init(){

        // 2. 启动监听容器
        container = StreamMessageListenerContainer
                .create(redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .build());

        for (RedisConsumerRegistrar r:registrars){
            for (RedisConsumerDef def : r.consumers()) {
                // 1. 创建消费者组（如果已存在会报错，直接忽略）
                try{
                    redisTemplate.opsForStream().createGroup(def.getStreamKey(), def.getGroup());
                }catch (Exception e){}

                container.receive(Consumer.from(def.getGroup(), def.getConsumer()),
                        StreamOffset.create(def.getStreamKey(), ReadOffset.lastConsumed()),
                        consumerListener);
            }
        }

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
