package com.userservice;

import com.userservice.entity.Rating;
import com.userservice.external.service.RatingServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceApplicationTests {

	@Autowired
	private RatingServiceTest rst;
	@Test
	void contextLoads() {
		Rating r = Rating.builder().ratingId("123").hotelId("123").userId("123").feedback("This Documnent creatted by test").rating(5).build();
		Rating res = rst.createRatingTest(r);
		if(res!=null){
			System.out.println("Rating created successfully");
		}else {
			System.out.println("Rating service Failed");
		}
	}

}
