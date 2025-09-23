package com.crawler.crawler_exercise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss.alibaba")
public class AlibabaConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
}
