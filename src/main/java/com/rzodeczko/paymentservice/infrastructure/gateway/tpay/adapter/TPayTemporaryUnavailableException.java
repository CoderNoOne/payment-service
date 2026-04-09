package com.rzodeczko.paymentservice.infrastructure.gateway.tpay.adapter;

/**
 * Signals a transient TPay outage or transport-level failure that may succeed on retry.
 */
public class TPayTemporaryUnavailableException extends RuntimeException {

    public TPayTemporaryUnavailableException(String message) {
        super(message);
    }

    public TPayTemporaryUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

