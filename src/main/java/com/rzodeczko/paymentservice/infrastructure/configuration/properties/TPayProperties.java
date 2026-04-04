package com.rzodeczko.paymentservice.infrastructure.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties mapped from the {@code tpay} namespace.
 *
 * @param api TPay API integration settings
 * @param app application callback and return URL settings used by TPay flows
 */
@ConfigurationProperties(prefix = "tpay")
public record TPayProperties(Api api, App app) {

    /**
     * API-level credentials and endpoints required for TPay communication.
     *
     * @param url base URL of the TPay API
     * @param clientId OAuth client identifier used to obtain access tokens
     * @param clientSecret OAuth client secret used for token acquisition
     * @param securityCode shared secret used to validate incoming notifications
     */
    public record Api(String url, String clientId, String clientSecret, String securityCode) {
    }

    /**
     * Application URLs exposed to TPay for redirects and asynchronous callbacks.
     *
     * @param notificationUrl public webhook endpoint for payment status notifications
     * @param returnSuccessUrl payer redirect URL used after successful payment completion
     * @param returnErrorUrl payer redirect URL used when payment processing fails
     */
    public record App(String notificationUrl, String returnSuccessUrl, String returnErrorUrl) {
    }
}
