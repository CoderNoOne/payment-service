package com.rzodeczko.paymentservice.presentation.dto;

/**
 * Payload returned by the health-check endpoint.
 *
 * @param message service health status message
 */
public record HealthCheckResponseDto(String message) {
}
