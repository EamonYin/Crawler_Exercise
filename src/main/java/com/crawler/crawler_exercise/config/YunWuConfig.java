package com.crawler.crawler_exercise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yunwu")
public class YunWuConfig {
    private String key;
}
