package com.crawler.crawler_exercise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;

public interface ICrawlerInfoService extends IService<CrawlerInfo> {

    public void insertCrawlerInfo(CrawlerInfo crawlerInfo);

    public String insertCrawlerInfoAndError(CrawlerInfo crawlerInfo);

    void updateWithLongTransaction(Long id, String info);

    boolean simulatePlaceOrderFailure(CrawlerInfo crawlerInfo1, CrawlerInfo crawlerInfo2);
}
