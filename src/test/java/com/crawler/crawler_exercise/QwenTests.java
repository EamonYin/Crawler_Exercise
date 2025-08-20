package com.crawler.crawler_exercise;

import com.crawler.crawler_exercise.config.EamonGPTConfig;
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
    @Autowired
    EamonGPTConfig eamonGPTConfig;

    @Test
    void testQwen(){
        qwenMsgService.sendSmsCN("15620964916");
    }

    @Test
    void testQwenRedis(){
        eamonGPTConfig.upDateEamonGPTKey("_5xnPLTiy8XbjMHU_4e0Mf5sxhQ_aWfJOtieR6Sek1*trtMUHEnF6AFvL_vsxhJp0");
    }



}
