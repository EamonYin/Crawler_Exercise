package com.crawler.crawler_exercise;

import com.crawler.crawler_exercise.config.YunWuConfig;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class CrawlerExerciseApplicationTests {

    @Resource
    ICrawlerInfoService crawlerInfoService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void mysqlTest() {
        CrawlerInfo crawlerInfo = new CrawlerInfo();
        crawlerInfo.setInfo("第一条数据");
        crawlerInfoService.insertCrawlerInfo(crawlerInfo);

    }

    @Test
    void testRedis() {
        // 写入数据
        redisTemplate.opsForValue().set("test_key", "Hello Spring Boot Redis!");

        // 读取数据
        String value = redisTemplate.opsForValue().get("test_key");

        System.out.println("Redis value: " + value);
    }

}
