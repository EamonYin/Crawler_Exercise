package com.crawler.crawler_exercise.utils.redisMQ.annotaion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DbUpdateListenerV2 {

    private static final Logger log = LoggerFactory.getLogger(DbUpdateListenerV2.class);

    @RedisStreamListener(
            streamKey = "demo:stream:db",
            group = "db-update-group-v2",
            consumer = "db-update-consumer-v2",
            autoAck = true
    )
    public void onMessage(Map<String, String> payload) {
//        if (!"DB_UPDATE".equals(payload.get("bizType"))) {
//            return;
//        }
        log.info("V2-模拟数据库更新：data={}", payload);
    }

    @RedisStreamListener(
            streamKey = "demo:stream:redis",
            group = "db-update-group-v2",
            consumer = "db-update-consumer-v2",
            autoAck = true
    )
    public void onMessageRdis(Map<String, String> payload) {
//        if (!"DB_UPDATE".equals(payload.get("bizType"))) {
//            return;
//        }
        log.info("V2-模拟Redis更新：data={}", payload);
    }
}
