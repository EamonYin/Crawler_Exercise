package com.crawler.crawler_exercise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crawler.crawler_exercise.entiy.QwenMsg;

public interface IQwenMsgService extends IService<QwenMsg> {
    String sendSmsCN(String phoneNum);

    String login(String phoneNum, String smsCode);
}
