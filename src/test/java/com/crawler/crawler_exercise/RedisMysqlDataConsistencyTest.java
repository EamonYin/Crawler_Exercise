package com.crawler.crawler_exercise;

import com.alibaba.fastjson.JSON;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.*;

/**
 * mysql & redis 数据一致性测试
 */
@SpringBootTest
@Slf4j
public class RedisMysqlDataConsistencyTest {

    @Resource
    ICrawlerInfoService crawlerInfoService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TaskScheduler taskScheduler;

    @Test
    void redisMysqlTest () throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        String redisKey = "Thread2Test";

        // 先操作数据库，再删除缓存！！！
        executor.submit(() -> {
            try{
                log.info("线程{}  更新数据库数据,时间:{}",Thread.currentThread().getName(),new Date());
                CrawlerInfo crawlerInfo = new CrawlerInfo();
                crawlerInfo.setId(1L);
                crawlerInfo.setInfo("新的修改"+new Date());
                crawlerInfoService.updateById(crawlerInfo);
                Thread.sleep(200);
                redisTemplate.delete(redisKey);
                log.info("线程{}  更新数据库数据完成,时间:{}",Thread.currentThread().getName(),new Date());
                // 延迟执行第二次删除
                // 延迟5秒后执行第二次删除
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.schedule(() -> {
                    redisTemplate.delete(redisKey);
                    log.info("线程1第二次删除缓存！");
                    scheduler.shutdown(); // 执行完后关闭调度器
                }, 5, TimeUnit.SECONDS); // 假设5秒后执行，实际应根据需求设置定时表达式
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        });


        executor.submit(() -> {
            log.info("线程2查询数据");
            if(redisTemplate.hasKey(redisKey)){
                log.info("线程{}  时间:{}  查询[redis]的结果是:{}",Thread.currentThread().getName(),new Date(),JSON.toJSONString(redisTemplate.opsForValue().get(redisKey)));
            }else {
                log.info("线程{}开始查询数据库，时间:{}",Thread.currentThread().getName(), new Date());
                CrawlerInfo byId = crawlerInfoService.getById(1L);
                log.info("线程{} 时间:{}  查询[数据库]的结果是:{}",Thread.currentThread().getName(),new Date(),JSON.toJSONString(byId));
                redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(byId));
            }
        });

        executor.submit(() -> {
            try {
                Thread.sleep(8000);
                log.info("线程3查询数据");
                if(redisTemplate.hasKey(redisKey)){
                    log.info("线程{}  时间:{}  查询[redis]的结果是:{} ",Thread.currentThread().getName(),new Date(),JSON.toJSONString(redisTemplate.opsForValue().get(redisKey)));
                }else {
                    log.info("线程{}开始查询数据库，时间:{}",Thread.currentThread().getName(), new Date());
                    CrawlerInfo byId = crawlerInfoService.getById(1L);
                    log.info("线程{}  时间:{}  查询[数据库]的结果是:{} ",Thread.currentThread().getName(),new Date(),JSON.toJSONString(byId));
                    redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(byId));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
    }

}
