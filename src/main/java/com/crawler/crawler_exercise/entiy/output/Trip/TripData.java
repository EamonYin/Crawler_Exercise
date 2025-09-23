package com.crawler.crawler_exercise.entiy.output.Trip;

import lombok.Data;

import java.util.List;

@Data
public class TripData {
    private List<TripDay> days;
}
