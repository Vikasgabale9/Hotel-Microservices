package com.userservice.exception;

import com.userservice.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException e){
        String message =e.getMessage();
       ApiResponse res=  ApiResponse.builder().message(message).success(false)
               .status(HttpStatus.NOT_FOUND).data(null).build();
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e){
        String message =e.getMessage();
        ApiResponse res=  ApiResponse.builder().message(message).success(false)
                .status(HttpStatus.INTERNAL_SERVER_ERROR).data(null).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR ).body(res);
    }
}
