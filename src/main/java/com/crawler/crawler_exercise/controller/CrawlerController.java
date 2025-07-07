package com.crawler.crawler_exercise.controller;


import com.crawler.crawler_exercise.config.TOBaiDu;
import com.crawler.crawler_exercise.config.TOEastmoney;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class CrawlerController {
    public static void main(String[] args) {
//        CrawlerController crawlerController = new CrawlerController();
//        crawlerController.seleniumProcess();

//        TOBaiDu toBaiDu = new TOBaiDu();
//        String str = toBaiDu.crawlerBaidu();
//        System.out.println("这里是Controller的输出:"+str);

        TOEastmoney toEastmoney = new TOEastmoney();
        String s = toEastmoney.seleniumProcess();
        System.out.println("这里是Controller的输出:"+s);

    }



}
