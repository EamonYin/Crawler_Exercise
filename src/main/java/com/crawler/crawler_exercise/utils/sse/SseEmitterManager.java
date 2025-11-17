package com.crawler.crawler_exercise.utils.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class SseEmitterManager {
    // Set<Wrapper> 代表并存的多个http链接；Wrapper 内的 eventName 代表“单连接只订阅一个事件”
    private final ConcurrentMap<String, Set<Wrapper>> emitters = new ConcurrentHashMap<>();

    private static class Wrapper {
        final SseEmitter emitter;
        final String eventName;
        Wrapper(SseEmitter emitter, String eventName) {
            this.emitter = emitter;
            this.eventName = eventName;
        }
    }

    // 创建一个新的Emitter
    public SseEmitter createEmitter(String id,String eventName) {
        SseEmitter emitter = new SseEmitter(0L);
        Wrapper wrapper = new Wrapper(emitter, eventName);
        // 为当前 id 取出连接集合，没有就创建并放入 map，然后加入当前连接
        Set<Wrapper> wrappers = emitters.get(id);
        if (wrappers == null) {
            Set<Wrapper> newWrappers = ConcurrentHashMap.newKeySet();
            // 当且仅当 当前 key 没有值（value 为 null）时才放入新的值，并返回旧值（如果没有旧值则返回 null）
            Set<Wrapper> existing = emitters.putIfAbsent(id, newWrappers);
            wrappers = (existing == null) ? newWrappers : existing;
        }
        wrappers.add(wrapper);
        // 连接结束时清理映射，避免重复发送
        emitter.onCompletion(() -> removeEmitter(id, wrapper));
        emitter.onTimeout(() -> removeEmitter(id, wrapper));
        emitter.onError(throwable -> removeEmitter(id, wrapper));
        return emitter;
    }

    // 发送消息
    public void sendMessage(String id, String message, String eventName) throws Exception {
        Set<Wrapper> wrappers = emitters.get(id);
        if (wrappers == null || wrappers.isEmpty()) {
            log.warn("未找到Emitter: {}", id);
            return;
        }
        for (Wrapper wrapper : wrappers) {
            if (!eventName.equals(wrapper.eventName)) {
                continue;
            }
            SseEmitter.SseEventBuilder builder = SseEmitter.event()
                    .id(id)
                    .name(eventName)
                    .data(message);
            wrapper.emitter.send(builder);
        }
    }

    private void removeEmitter(String id, Wrapper wrapper) {
        emitters.computeIfPresent(id, (key, wrappers) -> {
            wrappers.remove(wrapper);
            return wrappers.isEmpty() ? null : wrappers;
        });
    }
}
