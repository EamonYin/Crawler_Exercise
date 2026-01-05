package com.crawler.crawler_exercise.utils.redisMQ.spi_type;

import java.util.List;

public interface RedisConsumerRegistrar {
    List<RedisConsumerDef> consumers();
}
