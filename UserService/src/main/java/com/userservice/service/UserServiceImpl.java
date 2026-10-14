
package com.userservice.service;

import com.userservice.entity.Hotel;
import com.userservice.entity.Rating;
import com.userservice.entity.User;
import com.userservice.exception.ResourceNotFoundException;
import com.userservice.external.service.HotelService;
import com.userservice.external.service.RatingService;
import com.userservice.repo.UserRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final HotelService hotelService;
    private final RatingService ratingService;

    public UserServiceImpl(
            UserRepository userRepository,
            HotelService hotelService,
            RatingService ratingService) {

        this.userRepository = userRepository;
        this.hotelService = hotelService;
        this.ratingService = ratingService;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @CircuitBreaker(
            name = "ratingHotelBreaker",
            fallbackMethod = "ratingHotelFallback")
    public List<User> getAllUsers() {

        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<Rating> ratings =
                    ratingService.getAllRatingsByUserId(
                            user.getUserId());

            ratings.forEach(rating -> {
                Hotel hotel =
                        hotelService.getHotel(rating.getHotelId());

                rating.setHotel(hotel);
            });

            user.setRatings(ratings);
        }

        return users;
    }

    @Override
    @CircuitBreaker(
            name = "userByIdBreaker",
            fallbackMethod = "userByIdFallback")
    public User getUser(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invalid UserId " + userId));

        List<Rating> ratings =
                ratingService.getAllRatingsByUserId(userId);

        ratings.forEach(rating -> {
            Hotel hotel =
                    hotelService.getHotel(rating.getHotelId());

            rating.setHotel(hotel);
        });

        user.setRatings(ratings);

        logger.info("Ratings for user {}: {}", userId, ratings);

        return user;
    }

    @Override
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public User updateUser(User user) {

        User existingUser = userRepository.findById(user.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found " + user.getUserId()));

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setAbout(user.getAbout());

        return userRepository.save(existingUser);
    }

    // Fallback for getAllUsers()
    public List<User> ratingHotelFallback(Exception ex) {

        logger.error(
                "Failed to retrieve users with ratings and hotel details",
                ex);

        throw new IllegalStateException(
                "User data is temporarily unavailable. Please try again later.",
                ex);
    }

    // Fallback for getUser(userId)
    public User userByIdFallback(String userId, Exception ex) {

        logger.error(
                "Failed to retrieve user {} with ratings and hotel details",
                userId,
                ex);

        throw new IllegalStateException(
                "User data is temporarily unavailable. Please try again later.",
                ex);
    }
}
