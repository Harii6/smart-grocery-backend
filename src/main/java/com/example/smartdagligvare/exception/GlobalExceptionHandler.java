package com.example.smartdagligvare.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField() + " " +
                                error.getDefaultMessage()
                )
                .orElse("Invalid request");

        return createResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ApiError> handleInvalidJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Request body contains invalid JSON",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(
            ResponseStatusException.class
    )
    public ResponseEntity<ApiError> handleStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        String message = exception.getReason() == null
                ? "Request failed"
                : exception.getReason();

        return createResponse(
                exception.getStatusCode(),
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiError> handleExternalApi(
            RestClientException exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "External API request failed",
                exception
        );

        return createResponse(
                HttpStatus.BAD_GATEWAY,
                "An external service is unavailable",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Unexpected backend error",
                exception
        );

        return createResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred",
                request.getRequestURI()
        );
    }

    private ResponseEntity<ApiError> createResponse(
            HttpStatusCode status,
            String message,
            String path
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                message,
                path
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    public record ApiError(
            Instant timestamp,
            int status,
            String message,
            String path
    ) {
    }
}