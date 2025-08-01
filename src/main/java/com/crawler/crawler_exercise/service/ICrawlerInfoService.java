package com.crawler.crawler_exercise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;

public interface ICrawlerInfoService extends IService<CrawlerInfo> {

    public void insertCrawlerInfo(CrawlerInfo crawlerInfo);

    void updateWithLongTransactionV1(Long id, String info);

//    void updateWithLongTransactionV2(Long id, String info);

}
