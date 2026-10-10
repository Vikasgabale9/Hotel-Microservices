package com.userservice.entity;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Rating {
    private String ratingId;
    private String userId;
    private String hotelId;
    private String feedback;
    private int rating;

    private Hotel hotel;
}
