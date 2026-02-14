package com.crawler.crawler_exercise.entiy.input;

import lombok.Data;

import java.util.List;

@Data
public class SpringAIDemoKbInsertInput {
    private String content;
    private List<String> contents;
}
