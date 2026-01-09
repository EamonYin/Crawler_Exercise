package com.crawler.crawler_exercise.controller.redis;

import com.alibaba.fastjson.JSON;
import com.crawler.crawler_exercise.entiy.dto.RedisMQPayDTO;
import com.crawler.crawler_exercise.utils.redisMQ.annotain_type.AnnotainProducer;
import com.crawler.crawler_exercise.utils.redisMQ.easy_type.Producer;
import com.crawler.crawler_exercise.utils.redisMQ.spi_type.SPI_Producer;
import com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type.DelayAnnotainProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis/stream")
public class RedisStreamToMQController {

//    @Autowired
//    private RedisMQSender redisMQProducer;

    @Autowired
    private Producer producer;

    @Autowired
    private SPI_Producer SPIProducer;

    @Autowired
    private AnnotainProducer annotainProducer;

    @Autowired
    private DelayAnnotainProducer delayAnnotainProducer;

//    /**
//     * 效果说明：
//     * /redis/stream/send 发送一条消息到 demo:stream
//     * 两个监听器分别在各自消费组里消费同一条消息并打印中文日志（模拟两条业务逻辑）
//     * 你可以下一步：
//     *
//     * 启动服务后请求 GET /redis/stream/send，看控制台日志是否出现两条模拟业务日志。
//     * 如果要区分两种业务类型，我可以改成两个不同 stream 或在消息里加业务字段。
//     */
//    @GetMapping("/send")
//    public String sendMessage() {
//        Map<String, String> message = new HashMap<>();
//        message.put("orderId", "123");
//        message.put("event", "CREATE");
//        message.put("time", String.valueOf(System.currentTimeMillis()));
//        message.put("source", "demo");
//        /**
//         * 注意⚠️：要用streamKey区分业务。
//         * Redis Stream 的“消费者组”是消费侧概念，生产者只能 XADD 到 stream，并不能“直接发送到某个组”
//         *
//         * 消费者组的概念，2025-12-27理解：
//         * 消费者组是一种逻辑概念，用于将多个消费者实例组合在一起，形成一个消费组。
//         * 每个消费者实例都属于一个消费组，并且可以消费同一个流中的消息。
//         * 消费者组的主要目的是实现消息的负载均衡和故障恢复。
//         */
//        redisMQProducer.send("demo:stream", message);
//        return "发送成功";
//    }

    @GetMapping("/sendV3")
    public void sendMessageV3(){
        producer.send("自己的demo");
    }

    @GetMapping("/sendV4")
    public void sendMessageV4(){
        SPIProducer.send("order-stream","下订单了！");
        SPIProducer.send("trade-stream","付款了！");
    }

    @GetMapping("/sendV5")
    public void sendMessageV5(){
        annotainProducer.send("demo:mq:redis:order","orderId=1");
        RedisMQPayDTO redisMQPayDTO = new RedisMQPayDTO();
        redisMQPayDTO.setId(1);
        redisMQPayDTO.setPayOrderId("order-1");
        annotainProducer.send("demo:mq:redis:pay", JSON.toJSONString(redisMQPayDTO));
    }

    @GetMapping("/sendV6")
    public void sendMessageV6(){
        delayAnnotainProducer.send("delay:mq:redis:order","orderId=1",2000);
        RedisMQPayDTO redisMQPayDTO = new RedisMQPayDTO();
        redisMQPayDTO.setId(1);
        redisMQPayDTO.setPayOrderId("order-1");
        delayAnnotainProducer.send("delay:mq:redis:pay", JSON.toJSONString(redisMQPayDTO));
    }

}
