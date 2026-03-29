package com.rzodeczko.paymentservice.application.port.output;

public record GatewayResult(String redirectUrl, String externalTransactionId) {
}
