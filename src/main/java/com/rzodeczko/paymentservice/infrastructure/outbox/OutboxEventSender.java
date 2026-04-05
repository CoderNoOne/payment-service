package com.rzodeczko.paymentservice.infrastructure.outbox;


import com.rzodeczko.paymentservice.application.port.output.NotificationPort;
import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Delivers {@link OutboxEvent} records to the external notification boundary and persists
 * the delivery outcome.
 * <p>
 * This component is part of the outbox workflow. For each processed event it updates the
 * domain state to {@code SENT}, {@code PENDING} (retry), or {@code FAILED} (retry limit
 * exhausted), then stores the updated entity through {@link OutboxEventRepository}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventSender {
    private final NotificationPort notificationPort;
    private final OutboxEventRepository outboxEventRepository;

    /**
     * Attempts to deliver a single outbox event in its own transaction.
     * <p>
     * A delivery failure from the external call is handled locally: the event is marked as
     * failed and persisted, so processing of other events can continue. The method always
     * attempts to persist the final event state after the delivery attempt.
     *
     * @param event outbox event to deliver and update; expected to be managed and mutable
     * @implNote {@link Propagation#REQUIRES_NEW} creates a new transaction per event,
     * isolating commit/rollback boundaries between consecutive events.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(OutboxEvent event) {
        try {
            notificationPort.notifyExternalService(event.getOrderId(), event.getPaymentId());
            event.markSent();
            log.info("OutboxEvent sent. eventId={}, orderId={}", event.getId(), event.getOrderId());
        } catch (Exception e) {
            event.markFailed();
            log.error(
                    "OutboxEvent failed. eventId={}, orderId={}, retryCount={}, reason={}",
                    event.getId(),
                    event.getOrderId(),
                    event.getRetryCount(),
                    e.getMessage()
            );
        }
        // Persist final state regardless of delivery outcome.
        // Final state is SENT, PENDING (retry), or FAILED (retry limit exhausted).
        outboxEventRepository.save(event);
    }
}
