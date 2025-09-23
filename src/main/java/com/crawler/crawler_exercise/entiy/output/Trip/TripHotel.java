package com.crawler.crawler_exercise.entiy.output.Trip;

import lombok.Data;

import java.util.List;
@Data
public class TripHotel {
    private String name;
    private String id;
    private double rating;
    private List<String> photos;
    private String booking_url;
}