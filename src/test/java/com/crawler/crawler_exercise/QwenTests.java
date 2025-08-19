package com.crawler.crawler_exercise;

import com.crawler.crawler_exercise.service.IQwenMsgService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class QwenTests {
    @Autowired
    IQwenMsgService qwenMsgService;

    @Test
    void testQwen(){
        qwenMsgService.sendSmsCN("15620964916");
    }

}
