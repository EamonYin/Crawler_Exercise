package com.crawler.crawler_exercise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss.bytedance")
public class BytedanceConfig {
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
