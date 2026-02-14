package com.crawler.crawler_exercise.entiy.output;

import lombok.Data;

import java.util.List;

@Data
public class SpringAIDemoChatOutput {
    // 前端后续继续传该ID以保持同一会话记忆
    private String conversationId;
    private String answer;
    private List<String> usedTools;
    private List<String> sources;
}
