package com.crawler.crawler_exercise.entiy.output.Trip;

import lombok.Data;

import java.util.List;
@Data
public class TripDay {
    private int day;
    private String city;
    private List<TripHotel> hotel;
    private List<TripAttraction> attractions;
}