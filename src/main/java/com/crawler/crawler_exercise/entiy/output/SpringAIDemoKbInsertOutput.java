package com.crawler.crawler_exercise.entiy.output;

import lombok.Data;

@Data
public class SpringAIDemoKbInsertOutput {
    private Integer insertedCount;
    private String collectionName;
    private String message;
}
