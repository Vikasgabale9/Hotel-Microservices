package com.userservice.external.service;

import com.userservice.entity.Rating;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@FeignClient(name = "RATINGSERVICE")
public interface RatingService {

    @GetMapping("/ratings")
    public List<Rating> getAllRatings();

    @PostMapping("/ratings")
    public Rating createRating(Rating rating);

    @GetMapping("/ratings/user/{userId}")
    public List<Rating> getAllRatingsByUserId(@PathVariable String userId);

    @GetMapping("/ratings/hotel/{hotelId}")
    public List<Rating> getAllRatingsByHotelId(@PathVariable String hotelId);
}
