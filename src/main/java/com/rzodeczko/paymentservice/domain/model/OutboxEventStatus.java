package com.rzodeczko.paymentservice.domain.model;

/**
 * Processing status of an outbox event.
 */
public enum OutboxEventStatus {
    /** Event is waiting to be sent. */
    PENDING,

    /** Event has been sent to the upstream service. */
    SENT,

    /** Retry limit has been exceeded and manual intervention is required. */
    FAILED
}
