package com.crawler.crawler_exercise.entiy.output;

import lombok.Data;

import java.util.List;

@Data
public class SpringAIDemoChatOutput {
    private String answer;
    private List<String> usedTools;
    private List<String> sources;
}
