package com.userservice.controller;

import com.userservice.entity.User;
import com.userservice.payload.ApiResponse;
import com.userservice.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    // =========================================================
    // CREATE USER
    // =========================================================

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<User>> createUser(
            @RequestBody User user) {

        ApiResponse<User> res = ApiResponse.<User>builder()
                .message("User created successfully")
                .success(true)
                .status(HttpStatus.CREATED)
                .data(userService.saveUser(user))
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(res);
    }


    // =========================================================
    // GET ALL USERS
    // =========================================================

    @GetMapping("/getAll")
    @Retry(
            name = "ratingHotelService",
            fallbackMethod = "ratingHotelFallbackGetAll"
    )
    @RateLimiter(
            name = "userRateLimiter",
            fallbackMethod = "ratingHotelFallbackGetAll"
    )
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        ApiResponse<List<User>> res = ApiResponse.<List<User>>builder()
                .message("Data retrieved successfully")
                .success(true)
                .status(HttpStatus.OK)
                .data(userService.getAllUsers())
                .build();

        return ResponseEntity.ok(res);
    }


    // =========================================================
    // FALLBACK - GET ALL USERS
    // =========================================================

    public ResponseEntity<ApiResponse<List<User>>> ratingHotelFallbackGetAll(
            Throwable throwable) {

        ApiResponse<List<User>> res = ApiResponse.<List<User>>builder()
                .message("User service is temporarily unavailable")
                .success(false)
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .data(Collections.emptyList())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(res);
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    @GetMapping("/{userId}")
    @Retry(
            name = "userByIdRetry",
            fallbackMethod = "userByIdFallback"
    )
    @CircuitBreaker(
            name = "userByIdBreaker",
            fallbackMethod = "userByIdFallback"
    )
    @RateLimiter(
            name = "userByIdRateLimiter",
            fallbackMethod = "userByIdFallback"
    )
    public ResponseEntity<ApiResponse<User>> getUser(
            @PathVariable String userId) {

        ApiResponse<User> res = ApiResponse.<User>builder()
                .message("User retrieved successfully")
                .success(true)
                .status(HttpStatus.OK)
                .data(userService.getUser(userId))
                .build();

        return ResponseEntity.ok(res);
    }

    // =========================================================
    // FALLBACK - GET USER BY ID
    // =========================================================

    public ResponseEntity<ApiResponse<User>> userByIdFallback(
            String userId,
            Throwable throwable) {

        ApiResponse<User> res = ApiResponse.<User>builder()
                .message("User service is temporarily unavailable for user: " + userId)
                .success(false)
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .data(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(res);
    }


    // =========================================================
    // DELETE USER
    // =========================================================

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable String userId) {

        userService.deleteUser(userId);

        ApiResponse<String> res = ApiResponse.<String>builder()
                .message("User deleted successfully")
                .success(true)
                .status(HttpStatus.OK)
                .data("User ID: " + userId)
                .build();

        return ResponseEntity.ok(res);
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable String userId,
            @RequestBody User user) {

        user.setUserId(userId);

        ApiResponse<User> res = ApiResponse.<User>builder()
                .message("User updated successfully")
                .success(true)
                .status(HttpStatus.OK)
                .data(userService.updateUser(user))
                .build();

        return ResponseEntity.ok(res);
    }
}