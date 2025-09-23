package com.crawler.crawler_exercise.entiy.output.Trip;

import lombok.Data;

import java.util.List;
@Data
public class TripResponse {
    private int code;
    private String message;
    private TripData data;
}