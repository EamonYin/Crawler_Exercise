package com.crawler.crawler_exercise.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.mapper.CrawlerInfoMapper;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public void updateWithLongTransaction(Long id, String info) {
        try {
            log.info("开始长事务更新，ID: {}, 线程: {}", id, Thread.currentThread().getName());

            // 先查询并锁定行（SELECT FOR UPDATE）
            CrawlerInfo crawlerInfo = crawlerInfoMapper.selectById(id);
            if (crawlerInfo != null) {
                crawlerInfo.setInfo(info + "-" + System.currentTimeMillis());

                // 模拟长时间处理（持有行锁）
                Thread.sleep(5000); // 持有锁5秒

                // 执行更新
                crawlerInfoMapper.updateById(crawlerInfo);
                log.info("长事务更新完成，ID: {}, 线程: {}", id, Thread.currentThread().getName());
            } else {
                log.warn("未找到ID为{}的记录", id);
            }
        } catch (InterruptedException e) {
            log.error("长事务被中断: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("长事务执行失败: {}", e.getMessage());
            throw new RuntimeException("长事务执行失败", e);
        }
    }
}
