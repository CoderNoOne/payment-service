package com.rzodeczko.paymentservice.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing an outbox entry used to publish payment-related integration events.
 *
 * <p>The event is created as {@link OutboxEventStatus#PENDING}, then moved to
 * {@link OutboxEventStatus#SENT} or {@link OutboxEventStatus#FAILED} depending on processing
 * attempts.
 */
public class OutboxEvent {
    private final UUID id;
    private final UUID orderId;
    private final UUID paymentId;
    private OutboxEventStatus status;
    private int retryCount;
    private final Instant createdAt;
    private Instant processedAt;

    /** Maximum number of delivery attempts before the event is marked as failed. */
    private static final int MAX_RETRY_COUNT = 5;

    /**
     * Creates a fully initialized outbox event.
     *
     * @param id unique event identifier
     * @param orderId related order identifier
     * @param paymentId related payment identifier
     * @param status current outbox status
     * @param retryCount number of processing retries already performed
     * @param createdAt timestamp when the event was created
     * @param processedAt timestamp when the event was successfully processed; {@code null} otherwise
     */
    public OutboxEvent(
            UUID id,
            UUID orderId,
            UUID paymentId,
            OutboxEventStatus status,
            int retryCount,
            Instant createdAt,
            Instant processedAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * Factory method creating a new pending outbox event.
     *
     * @param orderId related order identifier
     * @param paymentId related payment identifier
     * @return newly created outbox event with {@code PENDING} status and zero retries
     */
    public static OutboxEvent create(UUID orderId, UUID paymentId) {
        return new OutboxEvent(
                UUID.randomUUID(),
                orderId,
                paymentId,
                OutboxEventStatus.PENDING,
                0,
                Instant.now(),
                null
        );
    }

    /** Marks this event as sent and stores processing timestamp. */
    public void markSent() {
        this.status = OutboxEventStatus.SENT;
        this.processedAt = Instant.now();
    }

    /**
     * Increments retry counter and marks this event as failed
     * when the maximum retry threshold is reached.
     */
    public void markFailed() {
        ++this.retryCount;
        if (this.retryCount >= MAX_RETRY_COUNT) {
            this.status = OutboxEventStatus.FAILED;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public OutboxEventStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
