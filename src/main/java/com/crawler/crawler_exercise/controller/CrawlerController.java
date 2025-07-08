package com.crawler.crawler_exercise.controller;


import com.crawler.crawler_exercise.config.TOBaiDu;
import com.crawler.crawler_exercise.config.TOEastmoney;
import com.crawler.crawler_exercise.config.TOV2EX;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
@Slf4j
public class CrawlerController {
    public static void main(String[] args) {
        String result = "";

//        CrawlerController crawlerController = new CrawlerController();
//        crawlerController.seleniumProcess();

//        TOBaiDu toBaiDu = new TOBaiDu();
//        String str = toBaiDu.crawlerBaidu();

//        TOEastmoney toEastmoney = new TOEastmoney();
//        result = toEastmoney.seleniumProcess();

        TOV2EX tov2EX = new TOV2EX();
        result = tov2EX.getV2EXInfo();

        log.info("这里是Controller的输出:{}", result);
    }



}
