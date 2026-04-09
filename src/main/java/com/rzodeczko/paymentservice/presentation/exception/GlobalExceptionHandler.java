package com.rzodeczko.paymentservice.presentation.exception;


import com.rzodeczko.paymentservice.domain.exception.InvalidNotificationSignatureException;
import com.rzodeczko.paymentservice.domain.exception.PaymentAlreadyExistsException;
import com.rzodeczko.paymentservice.domain.exception.PaymentConcurrentModificationException;
import com.rzodeczko.paymentservice.domain.exception.PaymentNotFoundException;
import com.rzodeczko.paymentservice.presentation.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Global exception handler for the REST layer.
 *
 * <p>Converts domain and technical exceptions into consistent HTTP responses
 * with an {@link ErrorResponseDto} payload.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Handles {@link PaymentNotFoundException}.
     *
     * @param noResourceFoundException business exception
     * @return HTTP 404 (Not Found) with exception details
     */

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handle(NoResourceFoundException noResourceFoundException) {
        log.warn("Resource not found: {}", noResourceFoundException.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(404, "Not Found", noResourceFoundException.getMessage()));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handle(PaymentNotFoundException paymentNotFoundException) {
        log.warn("Payment not found: {}", paymentNotFoundException.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(404, "Not Found", paymentNotFoundException.getMessage()));
    }

    /**
     * Handles {@link InvalidNotificationSignatureException}.
     *
     * @param invalidNotificationSignatureException business exception
     * @return HTTP 400 (Bad Request) with exception details
     */
    @ExceptionHandler(InvalidNotificationSignatureException.class)
    public ResponseEntity<ErrorResponseDto> handle(InvalidNotificationSignatureException invalidNotificationSignatureException) {
        log.warn("Invalid notification signature");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(400, "Bad Request", invalidNotificationSignatureException.getMessage()));
    }

    /**
     * Handles {@link PaymentAlreadyExistsException}.
     *
     * @param paymentAlreadyExistsException business exception
     * @return HTTP 409 (Conflict) with exception details
     */
    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handle(PaymentAlreadyExistsException paymentAlreadyExistsException) {
        log.warn("Payment already exists: {}", paymentAlreadyExistsException.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto(409, "Conflict", paymentAlreadyExistsException.getMessage()));
    }

    /**
     * Handles {@link PaymentConcurrentModificationException}.
     *
     * @param paymentConcurrentModificationException business exception
     * @return HTTP 409 (Conflict) with exception details
     */
    @ExceptionHandler(PaymentConcurrentModificationException.class)
    public ResponseEntity<ErrorResponseDto> handle(PaymentConcurrentModificationException paymentConcurrentModificationException) {
        log.warn("Concurrent modification: {}", paymentConcurrentModificationException.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto(409, "Conflict", paymentConcurrentModificationException.getMessage()));
    }

    /**
     * Handles {@link MethodArgumentNotValidException}.
     *
     * @param methodArgumentNotValidException request argument validation exception
     * @return HTTP 400 (Bad Request) with concatenated field validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handle(MethodArgumentNotValidException methodArgumentNotValidException) {
        String message = methodArgumentNotValidException
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(400, "Validation failed", message));
    }

    /**
     * Handles {@link IllegalStateException}, typically integration-related.
     *
     * @param illegalStateException state exception
     * @return HTTP 500 (Internal Server Error) with exception details
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handle(IllegalStateException illegalStateException) {
        log.error("Integration error: {}", illegalStateException.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(500, "Internal Server Error", illegalStateException.getMessage()));
    }

    /**
     * Fallback handler for all unhandled exceptions.
     *
     * @param exception unhandled exception
     * @return HTTP 500 (Internal Server Error) with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handle(Exception exception) {
        log.error("Unexpected error", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(500, "Internal Server Error", "Unexpected error"));
    }
}
