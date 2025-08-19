package com.crawler.crawler_exercise.config;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "eamongpt")
public class EamonGPTConfig {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String url;

    private String key;

    private final String redisKey = "eamonGptKey";

    public String getEamonGPTKey() {
        String key = redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isEmpty(key)) {
            redisTemplate.opsForValue().set(redisKey, this.key);
            key = this.key;
        }
        return key;
    }

    public void upDateEamonGPTKey(String eamongptKey){
        redisTemplate.opsForValue().set(redisKey, eamongptKey);
    }
}
