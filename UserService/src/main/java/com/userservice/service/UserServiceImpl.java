package com.userservice.service;

import com.userservice.entity.Hotel;
import com.userservice.entity.Rating;
import com.userservice.entity.User;
import com.userservice.exception.ResourceNotFoundException;
import com.userservice.external.service.HotelService;
import com.userservice.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    private final String RATINGSUSERURL =
            "http://RATINGSERVICE/ratings/user/";

    private final String HOTELRATINGSURL =
            "http://HOTELSERVICE/api/hotels/";

    private final Logger logger =
            LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private HotelService hotelService;

    public UserServiceImpl(
            UserRepository userRepository,
            RestTemplate restTemplate
    ) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {

        List<User> users = userRepository.findAll();

        for (User u : users) {

            List<Rating> ratings = get(
                    RATINGSUSERURL + u.getUserId(),
                    new ParameterizedTypeReference<List<Rating>>() {}
            );

        /*    ratings.forEach(rating ->
                    rating.setHotel(
                            get(
                                    HOTELRATINGSURL + rating.getHotelId(),
                                    Hotel.class
                            )
                    )
            );*/

            ratings.forEach(rating ->
                    rating.setHotel(
                            hotelService.getHotel(rating.getHotelId())
                    )
            );

            u.setRatings(ratings);
        }

        return users;
    }

    @Override
    public User getUser(String userId) {

        User u = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invalid UserId " + userId
                        )
                );

        List<Rating> ratings = get(
                RATINGSUSERURL + userId,
                new ParameterizedTypeReference<List<Rating>>() {}
        );

        ratings.forEach(rating ->
                rating.setHotel(
                        get(
                                HOTELRATINGSURL + rating.getHotelId(),
                                Hotel.class
                        )
                )
        );

        u.setRatings(ratings);

        logger.info("Ratings for user {}: {}", userId, ratings);

        return u;
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
                                "User not found " + user.getUserId()
                        )
                );

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setAbout(user.getAbout());

        return userRepository.save(existingUser);
    }

    // For normal objects
    public <R> R get(String url, Class<R> responseType) {

        return restTemplate.getForObject(
                url,
                responseType
        );
    }

    // For generic types like List<Rating>
    public <R> R get(
            String url,
            ParameterizedTypeReference<R> responseType
    ) {

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                responseType
        ).getBody();
    }
}