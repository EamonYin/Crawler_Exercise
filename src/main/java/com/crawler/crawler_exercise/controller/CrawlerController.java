package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.config.YunWuConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/crawler")
public class CrawlerController {

    @Resource
    YunWuConfig yunWuConfig;

    public static void main(String[] args) throws InterruptedException {
        String result = "";

//        CrawlerController crawlerController = new CrawlerController();
//        crawlerController.seleniumProcess();

//        TOBaiDu toBaiDu = new TOBaiDu();
//        String str = toBaiDu.crawlerBaidu();

//        TOEastmoney toEastmoney = new TOEastmoney();
//        result = toEastmoney.seleniumProcess();
//        log.info("这里是Controller的输出:{}", result);
//
//        TOV2EX tov2EX = new TOV2EX();
//        List<V2EXInfo> v2EXInfo = tov2EX.getV2EXInfo();
//        log.info("抓取V2EX标题:{}", JSON.toJSONString(v2EXInfo));




    }

    @GetMapping("/getKey")
    public void getKey(){
        System.out.println(yunWuConfig.getKey());
    }






}
