package com.ecommerce.auth.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecommerce.auth.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "EMAIL_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> validationErrors.putIfAbsent(
                error.getField(),
                error.getDefaultMessage()
        ));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                request.getRequestURI(),
                validationErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "Request body is missing or contains invalid JSON",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Unexpected error while processing {}",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ApiError> handleInvalidResetToken(
            InvalidPasswordResetTokenException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_PASSWORD_RESET_TOKEN",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(
            PasswordConfirmationMismatchException.class
    )
    public ResponseEntity<ApiError> handlePasswordMismatch(
            PasswordConfirmationMismatchException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "PASSWORD_CONFIRMATION_MISMATCH",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of(
                        "confirmPassword",
                        exception.getMessage()
                )
        );
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String code,
            String message,
            String path,
            Map<String, String> errors) {

        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                path,
                errors
        );

        return ResponseEntity
                .status(status)
                .body(apiError);
    }
}
