package com.rzodeczko.paymentservice.infrastructure.gateway.tpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the OAuth2 token response returned by the TPay API.
 *
 * @param accessToken bearer access token extracted from the {@code access_token}
 *                    JSON field and used for authenticated gateway requests
 */
public record OAuthResponseDto(
        @JsonProperty("access_token") String accessToken
) {
}
