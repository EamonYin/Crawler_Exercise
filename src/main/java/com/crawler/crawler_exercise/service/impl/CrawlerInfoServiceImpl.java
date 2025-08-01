package com.crawler.crawler_exercise.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.mapper.CrawlerInfoMapper;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CrawlerInfoServiceImpl extends ServiceImpl<CrawlerInfoMapper, CrawlerInfo> implements ICrawlerInfoService {
    @Autowired
    CrawlerInfoMapper crawlerInfoMapper;

    @Override
    public void insertCrawlerInfo(CrawlerInfo crawlerInfo) {
        try {
            crawlerInfoMapper.insert(crawlerInfo);
            log.info("CrawlerInfoServiceImpl-insertCrawlerInfo插入成功:{}", JSON.toJSONString(crawlerInfo));
        } catch (Exception e) {
            log.error("CrawlerInfoServiceImpl-insertCrawlerInfo插入失败:{}", e.getMessage());
        }
    }
}
