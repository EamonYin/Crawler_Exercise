package com.crawler.crawler_exercise.service.springAgent.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ToolTraceContext {

    // 使用 ThreadLocal 保存“当前请求线程”的工具调用轨迹，避免并发请求互相污染。
    // ThreadLocal.withInitial(ArrayList::new) 是懒初始化。第一次在某线程调用 usedTools.get()/sources.get() 时，才给这个线程创建自己的 List。
    private final ThreadLocal<List<String>> usedTools = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<String>> sources = ThreadLocal.withInitial(ArrayList::new);

    // 在一次新请求开始前清空上下文，确保只保留本次请求的轨迹。
    public void clear() {
        String key = currentThreadKey();
        log.info("【ToolTraceContext】clear开始, key={}", key);
        usedTools.get().clear();
        sources.get().clear();
        log.info("【ToolTraceContext】clear结束, key={}", key);
    }

    // 记录本次请求实际触发过的工具名，例如 knowledge_search / web_search / current_time。
    public void addTool(String toolName) {
        usedTools.get().add(toolName);
    }

    // 记录工具对应的信息来源，例如 milvus:collection / searxng / system-clock:Asia/Shanghai。
    public void addSource(String source) {
        sources.get().add(source);
    }

    // 返回本次请求内的工具调用列表，给接口响应使用。
    public List<String> getUsedTools() {
        return new ArrayList<>(usedTools.get());
    }

    // 返回本次请求内的信息来源列表，给接口响应使用。
    public List<String> getSources() {
        return new ArrayList<>(sources.get());
    }

    // 用线程ID+线程名作为当前请求线程标识。
    public String currentThreadKey() {
        Thread t = Thread.currentThread();
        return t.getId() + "-" + t.getName();
    }
}
