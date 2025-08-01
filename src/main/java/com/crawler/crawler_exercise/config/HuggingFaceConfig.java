package com.crawler.crawler_exercise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "huggingface")
public class HuggingFaceConfig {
    private String token;
}
