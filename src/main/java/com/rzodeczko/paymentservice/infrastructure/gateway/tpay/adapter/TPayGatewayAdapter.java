package com.rzodeczko.paymentservice.infrastructure.gateway.tpay.adapter;

import com.rzodeczko.paymentservice.application.port.input.NotificationCommand;
import com.rzodeczko.paymentservice.application.port.output.GatewayResult;
import com.rzodeczko.paymentservice.application.port.output.PaymentGatewayPort;
import com.rzodeczko.paymentservice.infrastructure.configuration.properties.TPayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Infrastructure adapter implementing {@link PaymentGatewayPort} using the TPay API.
 *
 * <p>The adapter encapsulates outbound HTTP communication details and exposes a
 * stable payment-gateway contract to the application layer.</p>
 */
@Component
@Slf4j
public class TPayGatewayAdapter implements PaymentGatewayPort {
    private final TPayProperties tPayProperties;
    private final RestClient restClient;

    /**
     * Creates a TPay gateway adapter with a RestClient bound to the configured API base URL.
     *
     * @param tPayProperties strongly typed TPay integration properties
     * @param restClientBuilder RestClient builder used to construct a gateway-scoped client
     */
    public TPayGatewayAdapter(TPayProperties tPayProperties, RestClient.Builder restClientBuilder) {
        this.tPayProperties = tPayProperties;
        this.restClient = restClientBuilder
                .baseUrl(tPayProperties.api().url())
                .build();
    }

    /**
     * Registers a new transaction in TPay.
     *
     * @param orderId internal order identifier bound to the payment attempt
     * @param amount transaction amount to be registered in the gateway
     * @param email payer email forwarded to the gateway
     * @param name payer name forwarded to the gateway
     * @return gateway registration result containing redirect URL and external transaction ID
     */
    @Override
    public GatewayResult registerTransaction(UUID orderId, BigDecimal amount, String email, String name) {
        return null;
    }

    /**
     * Verifies whether the TPay transaction is in a confirmed state.
     *
     * @param externalTransactionId gateway-side transaction identifier
     * @return {@code true} when the transaction is confirmed, otherwise {@code false}
     */
    @Override
    public boolean verifyTransactionConfirmed(String externalTransactionId) {
        return false;
    }

    /**
     * Verifies the authenticity of an incoming TPay notification.
     *
     * @param notification normalized notification payload received by the application
     * @return {@code true} when notification signature validation succeeds, otherwise {@code false}
     */
    @Override
    public boolean verifyNotificationSignature(NotificationCommand notification) {
        return false;
    }
}
