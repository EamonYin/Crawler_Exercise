package com.crawler.crawler_exercise;

import com.crawler.crawler_exercise.config.YunWuConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class CrawlerExerciseApplicationTests {

    @Resource
    YunWuConfig yunWuConfig;

    @Test
    void contextLoads() {

        System.out.println(yunWuConfig.getKey());

    }

}
