package com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type;

import java.util.UUID;

final class StreamZsetDelayKeys {
    static final String QUEUE_PREFIX = "streamZsetDelay:";

    private StreamZsetDelayKeys() {
    }

    static String delayQueueKey(String streamKey) {
        return QUEUE_PREFIX + streamKey;
    }

    static String encodePayload(String message) {
        String safeMessage = message == null ? "" : message;
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
