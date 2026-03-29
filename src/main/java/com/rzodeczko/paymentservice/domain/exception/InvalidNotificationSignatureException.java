package com.rzodeczko.paymentservice.domain.exception;

public class InvalidNotificationSignatureException extends RuntimeException {
    public InvalidNotificationSignatureException() {
        super("Invalid notification signature");
    }
}
