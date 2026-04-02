package com.rzodeczko.paymentservice.application.service;


import com.rzodeczko.paymentservice.application.port.input.InitPaymentResult;
import com.rzodeczko.paymentservice.application.port.input.NotificationCommand;
import com.rzodeczko.paymentservice.application.port.input.PaymentUseCase;
import com.rzodeczko.paymentservice.application.port.output.GatewayResult;
import com.rzodeczko.paymentservice.application.port.output.PaymentGatewayPort;
import com.rzodeczko.paymentservice.domain.exception.InvalidNotificationSignatureException;
import com.rzodeczko.paymentservice.domain.exception.PaymentNotFoundException;
import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.model.Payment;
import com.rzodeczko.paymentservice.domain.repository.OutboxEventRepository;
import com.rzodeczko.paymentservice.domain.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application service implementing payment use cases.
 *
 * <p>This service orchestrates domain objects, repositories, and external gateway ports.
 * It coordinates flow but keeps business rules in domain models and collaborating components.
 */
public class PaymentService implements PaymentUseCase {
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final OutboxEventRepository outboxEventRepository;

    /**
     * Creates a payment application service.
     *
     * @param paymentRepository     repository used to load and persist payments
     * @param paymentGatewayPort    port used to register transactions and verify gateway data
     * @param outboxEventRepository repository used to persist integration outbox events
     */
    public PaymentService(PaymentRepository paymentRepository, PaymentGatewayPort paymentGatewayPort, OutboxEventRepository outboxEventRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentGatewayPort = paymentGatewayPort;
        this.outboxEventRepository = outboxEventRepository;
    }

    /**
     * Initializes a payment for a given order.
     *
     * <p>If a payment already exists for the order, returns existing payment details.
     * Otherwise registers a new transaction in the gateway, creates a new pending payment,
     * persists it, and returns initialization data.
     *
     * @param orderId unique identifier of the order
     * @param amount  requested payment amount
     * @param email   customer email passed to the gateway
     * @param name    customer name passed to the gateway
     * @return payment initialization result containing payment ID and redirect URL
     */
    @Override
    public InitPaymentResult initPayment(UUID orderId, BigDecimal amount, String email, String name) {
        return paymentRepository
                .findByOrderId(orderId)
                .map(existing -> new InitPaymentResult(existing.getId(), existing.getRedirectUrl()))
                .orElseGet(() -> {
                    GatewayResult gatewayResult = paymentGatewayPort.registerTransaction(orderId, amount, email, name);
                    Payment payment = Payment.create(
                            orderId,
                            amount,
                            gatewayResult.externalTransactionId(),
                            gatewayResult.redirectUrl()
                    );
                    paymentRepository.save(payment);
                    return new InitPaymentResult(payment.getId(), payment.getRedirectUrl());
                });
    }

    /**
     * Handles payment provider notification (webhook).
     *
     * <p>The method validates signature, resolves the payment by external transaction ID,
     * enforces idempotency for already-paid payments, and then updates payment status based
     * on notification data. For successful notifications it additionally verifies transaction
     * status with the provider API and emits an outbox event.
     *
     * @param notification notification payload from payment gateway
     * @throws InvalidNotificationSignatureException when webhook signature is invalid
     * @throws PaymentNotFoundException              when no payment exists for provided external transaction ID
     */
    @Override
    public void handleNotification(NotificationCommand notification) {
        if (!paymentGatewayPort.verifyNotificationSignature(notification)) {
            throw new InvalidNotificationSignatureException();
        }

        Payment payment = paymentRepository
                .findByExternalTransactionId(notification.trId())
                .orElseThrow(() -> new PaymentNotFoundException(notification.trId()));

        if (payment.isPaid()) {
            return;
        }

        if ("TRUE".equalsIgnoreCase(notification.trStatus())) {
            boolean confirmed = paymentGatewayPort.verifyTransactionConfirmed(notification.trId());
            if (confirmed) {
                payment.confirm();
                paymentRepository.save(payment);
                outboxEventRepository.save(OutboxEvent.create(payment.getOrderId(), payment.getId()));
            }
        } else {
            payment.fail();
            paymentRepository.save(payment);
        }
    }
}
