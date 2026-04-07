package com.rzodeczko.paymentservice.presentation.dto;

/**
 * Generic payload returned by payment flow callback endpoints.
 *
 * @param message payment flow status message
 */
public record PaymentResponseDto(String message) {
}
