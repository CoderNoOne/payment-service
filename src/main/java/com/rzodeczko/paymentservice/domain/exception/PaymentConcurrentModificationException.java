package com.rzodeczko.paymentservice.domain.exception;

public class PaymentConcurrentModificationException extends RuntimeException{
    public PaymentConcurrentModificationException() {
        super("Payment was modified concurrently, please retry");
    }
}
