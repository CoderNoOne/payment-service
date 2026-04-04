package com.rzodeczko.paymentservice.infrastructure.gateway.tpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO aggregating callback configuration sent to the TPay transaction API.
 *
 * @param payerUrls payer-facing redirection URLs used after payment completion
 * @param notification server-to-server notification configuration for payment status updates
 */
public record CallbacksDto(
        @JsonProperty("payerUrls") PayerUrlsDto payerUrls,
        @JsonProperty("notification") NotificationDto notification
) {
}
