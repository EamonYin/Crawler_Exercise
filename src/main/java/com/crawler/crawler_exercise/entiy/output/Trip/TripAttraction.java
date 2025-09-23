package com.crawler.crawler_exercise.entiy.output.Trip;

import lombok.Data;

import java.util.List;
@Data
public class TripAttraction {
    private String name;
    private List<String> photos;
    private double rating;
    private String address;
    private String booking_url;
}