package com.rzodeczko.paymentservice.infrastructure.gateway.tpay.dto;

/**
 * DTO representing payer identity data sent to the payment gateway.
 *
 * @param email payer email address used by the gateway for payment context and communication
 * @param name payer display name associated with the transaction
 */
public record PayerDto(String email, String name) {
}
