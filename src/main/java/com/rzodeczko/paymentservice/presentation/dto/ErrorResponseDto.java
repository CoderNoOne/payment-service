package com.rzodeczko.paymentservice.presentation.dto;

import java.time.Instant;

/**
 * Standard error payload returned by REST endpoints.
 *
 * @param status HTTP status code
 * @param error HTTP reason phrase or error category
 * @param message human-readable error details
 * @param timestamp time when the error response was created
 */
public record ErrorResponseDto(int status, String error, String message, Instant timestamp) {
    /**
     * Creates an error payload and sets {@code timestamp} to {@link Instant#now()}.
     *
     * @param status HTTP status code
     * @param error HTTP reason phrase or error category
     * @param message human-readable error details
     */
    public ErrorResponseDto(int status, String error, String message) {
        this(status, error, message, Instant.now());
    }
}
