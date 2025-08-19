package com.crawler.crawler_exercise.config;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.crawler.crawler_exercise.entiy.QwenMsg;
import com.crawler.crawler_exercise.service.IQwenMsgService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "eamongpt")
public class EamonGPTConfig {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String url;

    private String key;

    private final String redisKey = "eamonGptKey";
    @Autowired
    private IQwenMsgService qwenMsgService;

    public String getEamonGPTKey() {
        String key = redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isEmpty(key)) {
            LambdaUpdateWrapper<QwenMsg> qwenMsgLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            qwenMsgLambdaUpdateWrapper.eq(QwenMsg::getDeFlg,0);
            List<QwenMsg> list = qwenMsgService.list(qwenMsgLambdaUpdateWrapper);
            if(CollectionUtils.isNotEmpty(list)){
                QwenMsg qwenMsg = list.get(0);
                redisTemplate.opsForValue().set(redisKey, qwenMsg.getTongyiSsoTicket());
                key = qwenMsg.getTongyiSsoTicket();
            }else {
                redisTemplate.opsForValue().set(redisKey, this.key);
                key = this.key;
            }
        }
        return key;
    }

    public void upDateEamonGPTKey(String eamongptKey){
        redisTemplate.opsForValue().set(redisKey, eamongptKey);
    }
}
