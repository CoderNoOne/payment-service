package com.rzodeczko.paymentservice.application.port.input;

/**
 * Command carrying data required to process a payment notification (webhook) from TPay.
 *
 *
 *
 * @param merchantId merchant identifier assigned by TPay
 * @param trId transaction identifier assigned by TPay; typically mapped to
 *             {@code externalTransactionId} in the {@code Payment} entity
 * @param trDate transaction date and time provided by TPay
 * @param trCrc merchant-defined correlation value set during transaction creation and returned
 *              unchanged by TPay; useful for linking webhook data to internal business context
 * @param trAmount total transaction amount
 * @param trPaid amount actually paid by the customer
 * @param trDesc transaction description provided during payment initialization
 * @param trStatus payment result status, usually {@code "TRUE"} or {@code "FALSE"}
 * @param trError error code returned when payment processing fails
 * @param trEmail customer email associated with the payment
 * @param md5Sum cryptographic signature of the webhook payload used for integrity validation
 */
public record NotificationCommand(
        String merchantId,
        String trId,
        String trDate,
        String trCrc,
        String trAmount,
        String trPaid,
        String trDesc,
        String trStatus,
        String trError,
        String trEmail,
        String md5Sum
) { }
