package com.rzodeczko.paymentservice.application.port.output;


import com.rzodeczko.paymentservice.application.port.input.NotificationCommand;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGatewayPort {
    GatewayResult registerTransaction(UUID orderId, BigDecimal amount, String email, String name);
    boolean verifyTransactionConfirmed(String externalTransactionId);
    boolean verifyNotificationSignature(NotificationCommand notification);
}
