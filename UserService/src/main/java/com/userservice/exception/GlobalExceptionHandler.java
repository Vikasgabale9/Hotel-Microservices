
package com.userservice.exception;

import com.userservice.payload.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Resource not found: 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(
            ResourceNotFoundException e) {

        return buildResponse(
                e.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    // Request body validation errors: 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(
            MethodArgumentNotValidException e) {

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    // Invalid parameter types: 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {

        return buildResponse(
                "Invalid value for parameter: " + e.getName(),
                HttpStatus.BAD_REQUEST
        );
    }

    // Missing required query parameter: 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingParameter(
            MissingServletRequestParameterException e) {

        return buildResponse(
                "Required parameter is missing: " + e.getParameterName(),
                HttpStatus.BAD_REQUEST
        );
    }

    // Invalid JSON or unreadable request body: 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleInvalidRequestBody(
            HttpMessageNotReadableException e) {

        return buildResponse(
                "Invalid request body or JSON format",
                HttpStatus.BAD_REQUEST
        );
    }

    // Constraint validation errors: 400
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(
            ConstraintViolationException e) {

        return buildResponse(
                e.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    // Unsupported HTTP method: 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException e) {

        return buildResponse(
                "HTTP method is not supported for this endpoint",
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    // Duplicate key: 409
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse> handleDuplicateKey(
            DuplicateKeyException e) {

        return buildResponse(
                "A record with the same key already exists",
                HttpStatus.CONFLICT
        );
    }

    // Database constraint violations: 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrity(
            DataIntegrityViolationException e) {

        return buildResponse(
                "The request conflicts with existing data or database constraints",
                HttpStatus.CONFLICT
        );
    }

    // Authentication failures: 401
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthentication(
            AuthenticationException e) {

        return buildResponse(
                "Authentication is required or invalid",
                HttpStatus.UNAUTHORIZED
        );
    }

    // Authorization failures: 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(
            AccessDeniedException e) {

        return buildResponse(
                "You do not have permission to access this resource",
                HttpStatus.FORBIDDEN
        );
    }

    // Other unexpected exceptions: 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {

        // Log the full exception internally; don't expose details to clients.
        e.printStackTrace();

        return buildResponse(
                "An unexpected internal server error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiResponse> buildResponse(
            String message,
            HttpStatus status) {

        ApiResponse response = ApiResponse.builder()
                .message(message)
                .success(false)
                .status(status)
                .data(null)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
