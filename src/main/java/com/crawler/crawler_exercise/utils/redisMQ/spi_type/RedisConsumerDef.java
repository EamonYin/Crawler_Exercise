package com.crawler.crawler_exercise.utils.redisMQ.spi_type;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RedisConsumerDef {
    private String streamKey;
    private String group;
    private String consumer;
}
