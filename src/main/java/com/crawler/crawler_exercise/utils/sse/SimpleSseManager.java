package com.crawler.crawler_exercise.utils.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SimpleSseManager {

    private final Map<String, SseEmitter> sseMap = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String id) {
        SseEmitter emitter = new SseEmitter(0L); // 不超时
        sseMap.put(id, emitter);
        return emitter;
    }

    public void sendMessage(String id, String message) throws IOException {
        SseEmitter emitter = sseMap.get(id);
        if (emitter == null) return;

        emitter.send(
                SseEmitter.event()
                        .id(id)
                        .data(message)
        );
    }
}
