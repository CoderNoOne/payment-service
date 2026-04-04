package com.rzodeczko.paymentservice.infrastructure.gateway.tpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO defining server-side notification settings for a TPay transaction.
 *
 * @param url public webhook endpoint that receives asynchronous payment status callbacks
 * @param email recipient address used by the provider for notification-related communication
 */
public record NotificationDto(
        @JsonProperty("url") String url,
        @JsonProperty("email") String email
) {
}
