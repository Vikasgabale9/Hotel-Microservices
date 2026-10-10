
package com.rating.controller;

import com.rating.entity.Rating;
import com.rating.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Create Rating
    @PostMapping
    public ResponseEntity<Rating> createRating(@RequestBody Rating rating) {

        Rating savedRating = ratingService.createRating(rating);

        return new ResponseEntity<>(savedRating, HttpStatus.CREATED);
    }

    // Get All Ratings
    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {

        List<Rating> ratings = ratingService.getAllRatings();

        return ResponseEntity.ok(ratings);
    }

    // Get Ratings By User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Rating>> getAllRatingsByUserId(
            @PathVariable String userId) {

        List<Rating> ratings =
                ratingService.getAllRatingsByUserId(userId);

        return ResponseEntity.ok(ratings);
    }

    // Get Ratings By Hotel ID
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Rating>> getAllRatingsByHotelId(
            @PathVariable String hotelId) {

        List<Rating> ratings =
                ratingService.getAllRatingsByHotelId(hotelId);

        return ResponseEntity.ok(ratings);
    }
}