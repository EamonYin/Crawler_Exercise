package com.crawler.crawler_exercise;

import com.alibaba.fastjson.JSON;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.mapper.CrawlerInfoMapper;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.Set;
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
    @Autowired
    private DataSource dataSource;

    @Autowired
    private CrawlerInfoMapper crawlerInfoMapper;

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

    @Test
    @Transactional
    void testMysqlRowLock(){
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        // 为每个线程创建独立的事务管理器
        DataSourceTransactionManager transactionManager1 = new DataSourceTransactionManager(dataSource);
        DataSourceTransactionManager transactionManager2 = new DataSourceTransactionManager(dataSource);
        
        // 创建事务定义
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        executor.submit(() -> {
            try {
                // 开启事务
                TransactionStatus status1 = transactionManager1.getTransaction(definition);
                System.out.println("线程1开启事务，时间：" + System.currentTimeMillis());
                
                CrawlerInfo crawlerInfo = new CrawlerInfo();
                crawlerInfo.setId(5L);
                crawlerInfo.setInfo("表锁测试1" + new Date().getTime());
                crawlerInfo.setDeFlag(0);
                
                // 使用Mapper执行插入操作
                crawlerInfoMapper.insert(crawlerInfo);
                System.out.println("线程1执行插入操作完成，但不提交事务，时间：" + System.currentTimeMillis());
                
//                // 故意不提交事务，让线程2被阻塞
//                Thread.sleep(5000); // 持有锁5秒钟
//
//                transactionManager1.commit(status1);
                System.out.println("线程1事务提交成功，时间：" + new Date());
            } catch (Exception e) {
                System.out.println("线程1 rollback! 错误信息: " + e.getMessage());
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                Thread.sleep(3000); // 等待第一个线程先执行
                System.out.println("线程2开始执行，尝试插入相同ID的数据，时间：" + new Date());
                
                // 开启事务
                TransactionStatus status2 = transactionManager2.getTransaction(definition);
                CrawlerInfo crawlerInfo = new CrawlerInfo();
                // 使用相同的ID，这样会尝试获取相同的行锁
                crawlerInfo.setId(5L); 
                crawlerInfo.setInfo("表锁测试2" + new Date().getTime());
                crawlerInfo.setDeFlag(0);
                
                // 这里会被阻塞，直到线程1释放锁
                long startTime = System.currentTimeMillis();
                crawlerInfoMapper.insert(crawlerInfo);
                long endTime = System.currentTimeMillis();
                
                System.out.println("线程2插入操作完成，耗时：" + (endTime - startTime) + "ms");
                transactionManager2.commit(status2);
                System.out.println("线程2事务提交成功");
            } catch (Exception e) {
                System.out.println("线程2 rollback! 错误信息: " + e.getMessage());
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }

    @Test
    void testRedisCounter(){

        String LIKE_RANK_KEY = "rank:article";
        record article(Integer userId,String name,String content){}
        article article1 = new article(1,"张三","这里是张三写的一篇水文，不知道有没有人看");
        article article2= new article(2,"Tianc","我是Tianc，我什么都会，尽管问我");
        article article3 = new article(3,"Eamon","哈哈哈哈哈哈，哈哈哈哈哈哈");

//        redisTemplate.opsForZSet().incrementScore(LIKE_RANK_KEY, String.valueOf(article1), 3);
        redisTemplate.opsForZSet().incrementScore(LIKE_RANK_KEY, String.valueOf(article2), 2);
//        redisTemplate.opsForZSet().incrementScore(LIKE_RANK_KEY, String.valueOf(article3), 1);

        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(LIKE_RANK_KEY, 0, 3 - 1);
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            System.out.println("Value: " + tuple.getValue() + ", Score: " + tuple.getScore());
        }
    }


    public class article{
        private Integer userId;
        private String name;
        private String content;
    }

}
