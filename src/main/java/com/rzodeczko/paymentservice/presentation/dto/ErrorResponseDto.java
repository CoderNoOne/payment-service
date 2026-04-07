package com.rzodeczko.paymentservice.presentation.dto;

import java.time.Instant;

public record ErrorResponseDto(int status, String error, String message, Instant timestamp) {
    public ErrorResponseDto(int status, String error, String message) {
        this(status, error, message, Instant.now());
    }
}
