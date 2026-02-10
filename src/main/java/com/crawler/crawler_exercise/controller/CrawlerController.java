package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.config.YunWuConfig;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/crawler")
public class CrawlerController {

    @Resource
    YunWuConfig yunWuConfig;
    @Autowired
    ICrawlerInfoService crawlerInfoService;

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

//
//        TOQwen toQwen = new TOQwen();
//        toQwen.loginInQwen();

    }

    @GetMapping("/testTransactional")
    public Map<String, Object> testTransactional(){
        Map<String, Object> result = new HashMap<>();

        CrawlerInfo crawlerInfo = new CrawlerInfo();
        crawlerInfo.setInfo("正确");
//
//        crawlerInfoService.insertCrawlerInfo(crawlerInfo);
//
        CrawlerInfo crawlerInfo2 = new CrawlerInfo();
        crawlerInfo2.setInfo("错误");
//
//        String s = crawlerInfoService.insertCrawlerInfoAndError(crawlerInfo2);
        try {
            // 下订单进[订单表] -> 调用第三方支付API -> 存[支付表]
            boolean b = crawlerInfoService.simulatePlaceOrderFailure(crawlerInfo, crawlerInfo2);
            log.info("[testTransactional]返回值:{}", b);
            result.put("code", 0);
            result.put("message", "success");
            result.put("data", b);
        } catch (Exception e) {
            log.error("[testTransactional]执行失败", e);
            result.put("code", 1);
            result.put("message", "下单失败，支付未完成，事务已回滚");
        }
        return result;
    }



}
