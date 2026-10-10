package com.userservice.external.service;


import com.userservice.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingServiceTest {

    public RatingServiceTest(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    private final RatingService ratingService;

    public Rating createRatingTest(Rating rating){
        return ratingService.createRating(rating);
    }

}
