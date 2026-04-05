package com.rzodeczko.paymentservice.infrastructure.notification.adapter;

import com.rzodeczko.paymentservice.application.port.output.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Infrastructure adapter implementing {@link NotificationPort} for outbound payment notifications.
 *
 * <p>The current implementation is intentionally minimal and logs notification attempts only.
 * It acts as a placeholder seam for the final integration (for example an HTTP API call or
 * asynchronous event publication) that will be introduced in later iterations.</p>
 */
@Component
@Slf4j
public class ExternalServiceNotificationAdapter implements NotificationPort {

    /**
     * {@inheritDoc}
     *
     * <p>This placeholder implementation performs structured logging and does not execute
     * a remote call yet.</p>
     */
    @Override
    public void notifyExternalService(UUID orderId, UUID paymentId) {
        log.info("Notifying external service with orderId: {} and paymentId: {}", orderId, paymentId);
    }
}
