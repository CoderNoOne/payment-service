package com.rzodeczko.paymentservice.domain.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String externalTransactionId) {
        super("Payment not found for external transaction id " + externalTransactionId);
    }

    public PaymentNotFoundException(UUID orderId) {
        super("Payment not found for order id " + orderId);
    }
}
