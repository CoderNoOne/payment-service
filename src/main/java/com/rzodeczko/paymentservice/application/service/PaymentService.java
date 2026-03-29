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

// Serwis odbiera polecenie, pyta domenę co zrobić, zapisuje wynik przez repozytoria, wywołuje porty zewnętrzne. Sam
// nic nie liczy i nic nie decyduje — tylko koordynuje.
public class PaymentService implements PaymentUseCase {
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final OutboxEventRepository outboxEventRepository;

    public PaymentService(PaymentRepository paymentRepository, PaymentGatewayPort paymentGatewayPort, OutboxEventRepository outboxEventRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentGatewayPort = paymentGatewayPort;
        this.outboxEventRepository = outboxEventRepository;
    }

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

    @Override
    public void handleNotification(NotificationCommand notification) {
        if (!paymentGatewayPort.verifyNotificationSignature(notification)) {
            throw new InvalidNotificationSignatureException();
        }

        Payment payment = paymentRepository
                .findByExternalTransactionId(notification.trId())
                .orElseThrow(() -> new PaymentNotFoundException(notification.trId()));

        // Idempotency check — TPay może wysłać to samo powiadomienie wielokrotnie
        if (payment.isPaid()) {
            return;
        }

        if ("TRUE".equalsIgnoreCase(notification.trStatus())) {
            // Podwójna weryfikacja przez API TPay — nie ufamy ślepo webhookowi
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
