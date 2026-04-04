package com.rzodeczko.paymentservice.infrastructure.gateway.tpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing payer redirection URLs used by the payment gateway.
 *
 * @param success URL to which the payer is redirected after successful payment processing
 * @param error URL to which the payer is redirected when payment processing fails
 */
public record PayerUrlsDto(
        @JsonProperty("success") String success,
        @JsonProperty("error") String error
) {
}
