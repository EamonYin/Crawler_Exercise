package com.crawler.crawler_exercise;

import com.crawler.crawler_exercise.config.YunWuConfig;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class CrawlerExerciseApplicationTests {

    @Resource
    ICrawlerInfoService crawlerInfoService;

    @Test
    void mysqlTest() {
        CrawlerInfo crawlerInfo = new CrawlerInfo();
        crawlerInfo.setInfo("第一条数据");
        crawlerInfoService.insertCrawlerInfo(crawlerInfo);

    }

}
