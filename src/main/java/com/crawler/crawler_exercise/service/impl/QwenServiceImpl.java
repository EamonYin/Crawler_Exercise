package com.crawler.crawler_exercise.service.impl;

import com.crawler.crawler_exercise.config.EamonGPTConfig;
import com.crawler.crawler_exercise.service.IQwenService;
import com.crawler.crawler_exercise.utls.api.QwenAPi;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class QwenServiceImpl implements IQwenService {

    @Resource
    EamonGPTConfig eamonGPTConfig;

    @Override
    public String sendSmsCN(String phoneNum) {
        QwenAPi qwenAPi = new QwenAPi();
        try {
            // 返回 smstoken
            return qwenAPi.sendSms("86", phoneNum, "CN");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String loginCN(String phoneNum, String smsToken, String smsCode) {
        QwenAPi qwenAPi = new QwenAPi();
        try {
            String tongyiSsoTicket = qwenAPi.login("86", phoneNum, "CN", smsToken, smsCode);
            eamonGPTConfig.upDateEamonGPTKey(tongyiSsoTicket);
            return tongyiSsoTicket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
