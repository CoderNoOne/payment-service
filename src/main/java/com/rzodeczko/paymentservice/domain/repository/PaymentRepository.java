package com.rzodeczko.paymentservice.domain.repository;


import com.rzodeczko.paymentservice.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findByExternalTransactionId(String externalTransactionId);

    Optional<Payment> findByOrderId(UUID orderId);
}
