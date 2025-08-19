package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.entiy.input.QwenLoginInput;
import com.crawler.crawler_exercise.entiy.input.QwenSendSmsInput;
import com.crawler.crawler_exercise.service.IQwenMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/qwen")
public class QwenController {

    @Autowired
    private IQwenMsgService qwenMsgService;

    @PostMapping("/sendSms")
    public String sendSms(@RequestBody QwenSendSmsInput input){
        return qwenMsgService.sendSmsCN(input.getPhoneNum());
    }

    @PostMapping("/login")
    public String login(@RequestBody QwenLoginInput input){
        return qwenMsgService.login(input.getPhoneNum(),input.getSmsCode());
    }

}
