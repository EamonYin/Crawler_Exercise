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
import org.springframework.util.ErrorHandler;

import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name = "redis.mq.mode", havingValue = "spi") //用于切换 annotain / spi / easy 三种redis实现
public class SPI_ContainerConfig {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ConsumerListener consumerListener;

    @Autowired
    private List<RedisConsumerRegistrar> registrars;
    @Autowired
    private ErrorHandler errorHandler;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @PostConstruct
    public void init(){

        // 2. 启动监听容器
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .errorHandler(errorHandler) //吞掉停止项目时的报错（详见com.crawler.crawler_exercise.utils.redisMQ.easy_type.ContainerConfig讲解）
                        .build();

        container = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);

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

}
