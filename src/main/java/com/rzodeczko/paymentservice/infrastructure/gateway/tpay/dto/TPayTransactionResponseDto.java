package com.rzodeczko.paymentservice.infrastructure.gateway.tpay.dto;

/**
 * DTO representing the transaction creation response returned by the TPay API.
 *
 * @param transactionPaymentUrl gateway URL where the payer is redirected to continue payment
 * @param transactionId provider-side transaction identifier used for further verification
 * @param status transaction status returned by the provider at response time
 */
public record TPayTransactionResponseDto(
        String transactionPaymentUrl,
        String transactionId,
        String status
) {
}
