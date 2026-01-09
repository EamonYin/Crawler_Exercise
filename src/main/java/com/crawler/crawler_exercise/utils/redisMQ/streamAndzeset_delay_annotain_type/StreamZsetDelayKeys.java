package com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type;

import java.util.UUID;

// 与 annotain_type 不同：延时模式需要 ZSET 队列 key 规则与唯一化的成员编码。
final class StreamZsetDelayKeys {
    static final String QUEUE_PREFIX = "streamZsetDelay:";

    private StreamZsetDelayKeys() {
    }

    static String delayQueueKey(String streamKey) {
        // delay 队列 key 由 streamKey 派生，便于按 stream 粒度扫描。
        return QUEUE_PREFIX + streamKey;
    }

    static String encodePayload(String message) {
        String safeMessage = message == null ? "" : message;
        // ZSET 成员需唯一，避免相同消息互相覆盖。
        return UUID.randomUUID() + "|" + safeMessage;
    }

    static String decodePayload(String member) {
        if (member == null) {
            return "";
        }
        int idx = member.indexOf('|');
        if (idx < 0) {
            return member;
        }
        if (idx == member.length() - 1) {
            return "";
        }
        return member.substring(idx + 1);
    }
}
