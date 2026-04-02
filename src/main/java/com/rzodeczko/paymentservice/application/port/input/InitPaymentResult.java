package com.rzodeczko.paymentservice.application.port.input;

import java.util.UUID;


/**
 * Result of payment initialization returned by the application layer.
 *
 * @param paymentId unique identifier of the created payment
 * @param redirectUrl URL to which the client should be redirected
 *                    to complete the payment flow
 */
public record InitPaymentResult(UUID paymentId, String redirectUrl) {
}
