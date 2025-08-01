package com.crawler.crawler_exercise;

import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
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
