package com.crawler.crawler_exercise;

import com.crawler.crawler_exercise.config.YunWuConfig;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class CrawlerExerciseApplicationTests {

    @Resource
    ICrawlerInfoService crawlerInfoService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void mysqlTest() {
        CrawlerInfo crawlerInfo = new CrawlerInfo();
        crawlerInfo.setInfo("第一条数据");
        crawlerInfoService.insertCrawlerInfo(crawlerInfo);

    }

    @Test
    void testRedis() {
        // 写入数据
        redisTemplate.opsForValue().set("test_key", "Hello Spring Boot Redis!");

        // 读取数据
        String value = redisTemplate.opsForValue().get("test_key");

        System.out.println("Redis value: " + value);
    }


    @Test
    void threadPoolExample(){
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 提交两个任务
        executor.submit(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("线程池任务1 - " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        executor.submit(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("线程池任务2 - " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // 关闭线程池
        executor.shutdown();
    }

    @Test
    void threadPoolExampleV2() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2); // 等待2个任务完成

        executor.submit(() -> {
            try {
                CrawlerInfo crawlerInfo = new CrawlerInfo();
                crawlerInfo.setInfo("第一条数据");
                crawlerInfoService.insertCrawlerInfo(crawlerInfo);
                System.out.println("线程1插入完成");
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                CrawlerInfo crawlerInfo = new CrawlerInfo();
                crawlerInfo.setInfo("第二条数据");
                crawlerInfoService.insertCrawlerInfo(crawlerInfo);
                System.out.println("线程2插入完成");
            } finally {
                latch.countDown();
            }
        });

        // 等待所有任务完成
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        System.out.println("所有插入任务完成");
    }

     @Test
     void highConcurrencyInsertTest() throws InterruptedException {
         int threadCount = 50; // 并发线程数
         ExecutorService executor = Executors.newFixedThreadPool(threadCount);
         CountDownLatch latch = new CountDownLatch(threadCount);

         long startTime = System.currentTimeMillis();

         for (int i = 0; i < threadCount; i++) {
             final int taskId = i;
             executor.submit(() -> {
                 try {
                     CrawlerInfo crawlerInfo = new CrawlerInfo();
                     crawlerInfo.setInfo("高并发数据-任务" + taskId + "-时间:" + System.currentTimeMillis());
                     crawlerInfoService.insertCrawlerInfo(crawlerInfo);
                     System.out.println("任务" + taskId + "插入完成");
                 } catch (Exception e) {
                     System.err.println("任务" + taskId + "执行失败: " + e.getMessage());
                 } finally {
                     latch.countDown();
                 }
             });
         }

         // 等待所有任务完成
         latch.await(30, TimeUnit.SECONDS);
         executor.shutdown();

         long endTime = System.currentTimeMillis();
         System.out.println("高并发插入完成，总耗时: " + (endTime - startTime) + "ms");
     }

    /**
     * 模拟mysql行锁异常
     * @throws InterruptedException
     */
    @Test
    void testMysqlRowLockWithTransaction() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        // 线程1：开启事务并持有行锁
        executor.submit(() -> {
            try {
                System.out.println("线程1开始事务，时间：" + System.currentTimeMillis());
                // 这里需要在Service层添加@Transactional方法来模拟长事务
                crawlerInfoService.updateWithLongTransactionV1(1L, "线程1长事务更新");
                System.out.println("线程1事务完成，时间：" + System.currentTimeMillis());
            } catch (Exception e) {
                System.err.println("线程1失败: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        // 线程2：尝试更新同一行（会被阻塞）
        executor.submit(() -> {
            try {
                Thread.sleep(500);
                System.out.println("线程2开始更新，时间：" + System.currentTimeMillis());
                CrawlerInfo updateData = new CrawlerInfo();
                updateData.setId(1L);
                updateData.setInfo("线程2快速更新");
                crawlerInfoService.updateById(updateData);
                System.out.println("线程2更新完成，时间：" + System.currentTimeMillis());
            } catch (Exception e) {
                System.err.println("线程2失败: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
    }

}
