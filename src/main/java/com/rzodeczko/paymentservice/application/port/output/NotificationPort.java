package com.rzodeczko.paymentservice.application.port.output;

import java.util.UUID;

public interface NotificationPort {
    void notifyExternalService(UUID orderId, UUID paymentId);
}
