package com.crawler.crawler_exercise.service;

public interface IQwenService {
    String sendSmsCN(String phoneNum);

    String loginCN(String phoneNum, String smsToken, String smsCode);
}
