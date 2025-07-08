package com.crawler.crawler_exercise.controller;


import com.alibaba.fastjson.JSON;
import com.crawler.crawler_exercise.config.TOBaiDu;
import com.crawler.crawler_exercise.config.TOEastmoney;
import com.crawler.crawler_exercise.config.TOV2EX;
import com.crawler.crawler_exercise.entiy.V2EXInfo;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

@Slf4j
public class CrawlerController {
    public static void main(String[] args) throws InterruptedException {
        String result = "";

//        CrawlerController crawlerController = new CrawlerController();
//        crawlerController.seleniumProcess();

//        TOBaiDu toBaiDu = new TOBaiDu();
//        String str = toBaiDu.crawlerBaidu();

//        TOEastmoney toEastmoney = new TOEastmoney();
//        result = toEastmoney.seleniumProcess();
//        log.info("这里是Controller的输出:{}", result);

        TOV2EX tov2EX = new TOV2EX();
        List<V2EXInfo> v2EXInfo = tov2EX.getV2EXInfo();
        log.info("抓取V2EX标题:{}", JSON.toJSONString(v2EXInfo));

    }



}
