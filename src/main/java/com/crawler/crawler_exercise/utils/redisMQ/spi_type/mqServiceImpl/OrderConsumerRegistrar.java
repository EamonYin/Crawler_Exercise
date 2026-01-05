package com.crawler.crawler_exercise.utils.redisMQ.spi_type.mqServiceImpl;

import com.crawler.crawler_exercise.utils.redisMQ.spi_type.RedisConsumerDef;
import com.crawler.crawler_exercise.utils.redisMQ.spi_type.RedisConsumerRegistrar;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderConsumerRegistrar implements RedisConsumerRegistrar {
    @Override
    public List<RedisConsumerDef> consumers() {
        return List.of(new RedisConsumerDef("order-stream", "order-group", "c1"));
    }
}
