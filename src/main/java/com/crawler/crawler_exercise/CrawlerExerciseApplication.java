package com.crawler.crawler_exercise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.crawler.crawler_exercise.mapper")
public class CrawlerExerciseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerExerciseApplication.class, args);
    }

}
