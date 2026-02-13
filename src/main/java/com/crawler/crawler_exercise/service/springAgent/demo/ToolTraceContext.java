package com.crawler.crawler_exercise.service.springAgent.demo;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ToolTraceContext {

    private final ThreadLocal<List<String>> usedTools = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<String>> sources = ThreadLocal.withInitial(ArrayList::new);

    public void clear() {
        usedTools.get().clear();
        sources.get().clear();
    }

    public void addTool(String toolName) {
        usedTools.get().add(toolName);
    }

    public void addSource(String source) {
        sources.get().add(source);
    }

    public List<String> getUsedTools() {
        return new ArrayList<>(usedTools.get());
    }

    public List<String> getSources() {
        return new ArrayList<>(sources.get());
    }
}
