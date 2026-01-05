package com.crawler.crawler_exercise.utils.redisMQ;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Slf4j
@Component
public class RedisStreamErrorHandler implements ErrorHandler {

    @Override
    public void handleError(Throwable t) {
        // 停机噪音：StreamPollTask -> DefaultStreamMessageListenerContainer -> RedisTemplate.execute
        // -> LettuceStreamCommands.xReadGroup -> LettuceConnection.await -> RedisSystemException
        // 根因是 RedisException "Connection closed"（channelInactive/cancelCommands 触发）。
        if (isConnectionClosed(t)) {
            log.debug("Ignore Redis connection closed.", t);
            return;
        }
        log.error("Unexpected error in Redis Stream listener.", t);
    }

    private boolean isConnectionClosed(Throwable t) {
        boolean hasRedisType = false;
        boolean hasConnectionClosed = false;
        Throwable current = t;
        while (current != null) {
            if (current instanceof RedisException || current instanceof RedisSystemException) {
                hasRedisType = true;
            }
            String message = current.getMessage();
            if (message != null && message.contains("Connection closed")) {
                hasConnectionClosed = true;
            }
            current = current.getCause();
        }
        return hasRedisType && hasConnectionClosed;
    }
}
