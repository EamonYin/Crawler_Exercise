package com.crawler.crawler_exercise.utils.redisMQ.streamAndzeset_delay_annotain_type;

import java.util.UUID;

final class StreamZsetDelayKeys {
    static final String QUEUE_PREFIX = "streamZsetDelay:";
    static final String QUEUE_SUFFIX = ":delay";
    private static final String MEMBER_PREFIX = "v1|";

    private StreamZsetDelayKeys() {
    }

    static String delayQueueKey(String streamKey) {
        return QUEUE_PREFIX + streamKey + QUEUE_SUFFIX;
    }

    static String encodePayload(String message) {
        String safeMessage = message == null ? "" : message;
        return MEMBER_PREFIX + UUID.randomUUID() + "|" + safeMessage;
    }

    static String decodePayload(String member) {
        if (member == null) {
            return "";
        }
        if (!member.startsWith(MEMBER_PREFIX)) {
            return member;
        }
        int idx = member.indexOf('|', MEMBER_PREFIX.length());
        if (idx < 0 || idx == member.length() - 1) {
            return "";
        }
        return member.substring(idx + 1);
    }
}
