package com.crawler.crawler_exercise.utils.redisMQ.easy_type;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Component
@Slf4j
@ConditionalOnProperty(name = "redis.mq.mode", havingValue = "easy") //用于切换 annotain / spi / easy 三种redis实现
public class ContainerConfig {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ConsumerListener consumerListener;
    /**
     * Redis Stream 监听线程（cTaskExecutor-*）在轮询 XREADGROUP 时，Redis 连接已关闭，
     * 导致 RedisSystemException 包装的 RedisException: Connection closed 被 StreamPollTask 抛出并记录为 ERROR。
     *
     * 关键堆栈（精简主链）：
     *
     * StreamPollTask.run → StreamPollTask.readRecords → DefaultStreamMessageListenerContainer.lambda$getReadFunction$4
     * RedisTemplate.execute → LettuceStreamCommands.xReadGroup
     * LettuceConnection.await → LettuceConnection.convertLettuceAccessException
     * RedisSystemException: Redis exception
     * Caused by: RedisException: Connection closed（DefaultEndpoint.cancelCommands / CommandHandler.channelInactive）
     */
    @Autowired
    private ErrorHandler errorHandler;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    /**
     * @PostConstruct注解解析
     * Spring 官方推荐的“安全初始化点”
     * Spring 启动完成、Bean 注入后、执行这个注解下的方法（自动启动 Redis Stream 的监听，让 MQ 从一开始就“活着”）
     * 可以确保 @Autowired 注入完成，即redisTemplate、consumer不为空
    **/
    @PostConstruct
    public void init(){
        // 1. 创建消费者组（如果已存在会报错，直接忽略）
        try{
            /**
             * 【XGROUP CREATE 创建消费者组命令】
             *
             * 下面的代码等价于
             * XGROUP CREATE demo-stream demo-group $
             *
             * $ 的意思是：
             * 历史消息不要了，只看以后新来的
             */
            redisTemplate.opsForStream().createGroup(RedisMqConst.STREAM_KEY, RedisMqConst.GROUP);
        }catch (Exception e){}
        // 2. 启动监听容器
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer
                        .StreamMessageListenerContainerOptions
                        .builder()
                        .errorHandler(errorHandler) //吞掉停止项目时的报错
                        .build();

        container = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);

        /**
         * 注册监听规则
         */
        container.receive(Consumer.from(RedisMqConst.GROUP,RedisMqConst.CONSUMER),
                StreamOffset.create(RedisMqConst.STREAM_KEY, ReadOffset.lastConsumed()),
                consumerListener);

        /**
         * 【XREADGROUP 监听 / 读取消息命令】
         *
         * 下面的代码等价于
         * XREADGROUP
         *   GROUP <group> <consumer>
         *   COUNT <n>
         *   BLOCK <ms>
         *   STREAMS <stream-key> <id>
         * XREADGROUP GROUP demo-group consumer-1 COUNT 1 BLOCK 0 STREAMS demo-stream >
         * 结尾“>”含义：只读取这个【消费者组】从未处理过的新消息
         */
        container.start();
    }

}
