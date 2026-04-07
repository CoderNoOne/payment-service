package com.rzodeczko.paymentservice.presentation.dto;

import java.util.UUID;

/**
 * Response payload returned after payment initialization.
 *
 * @param paymentId unique identifier of the created payment
 * @param redirectUrl URL where the client should be redirected to continue payment
 */
public record InitPaymentResponseDto(UUID paymentId, String redirectUrl) {
}
