package com.rzodeczko.paymentservice.infrastructure.persistence.adapter;

import com.rzodeczko.paymentservice.domain.exception.PaymentAlreadyExistsException;
import com.rzodeczko.paymentservice.domain.model.Payment;
import com.rzodeczko.paymentservice.domain.repository.PaymentRepository;
import com.rzodeczko.paymentservice.infrastructure.persistence.mapper.PaymentMapper;
import com.rzodeczko.paymentservice.infrastructure.persistence.repository.JpaPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements the domain {@link PaymentRepository}
 * contract using Spring Data JPA.
 *
 * <p>This adapter is responsible for mapping between domain objects and JPA
 * entities and for translating infrastructure exceptions into domain-specific
 * exceptions.</p>
 */
@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {
    private final JpaPaymentRepository jpaPaymentRepository;
    private final PaymentMapper paymentMapper;

    /**
     * Persists a payment aggregate in the database.
     *
     * <p>Uses {@code saveAndFlush} to execute SQL immediately so integrity
     * violations are raised within this adapter and can be mapped to
     * domain-level exceptions.</p>
     *
     * @param payment payment aggregate to persist
     * @return persisted payment aggregate
     * @throws PaymentAlreadyExistsException when uniqueness constraints are violated
     */
    @Override
    @Transactional
    public Payment save(Payment payment) {
        try {
            // Flush immediately so constraint violations are thrown in this layer and can be translated to domain exceptions
            return paymentMapper.toDomain(
                    jpaPaymentRepository.saveAndFlush(paymentMapper.toEntity(payment))
            );
        } catch (DataIntegrityViolationException e) {
            throw new PaymentAlreadyExistsException(payment.getOrderId());
        }
    }

    /**
     * Finds a payment by external transaction identifier assigned by the gateway.
     *
     * @param externalTransactionId gateway-side transaction identifier
     * @return optional containing the matching payment when found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByExternalTransactionId(String externalTransactionId) {
        return jpaPaymentRepository
                .findByExternalTransactionId(externalTransactionId)
                .map(paymentMapper::toDomain);
    }

    /**
     * Finds a payment by internal order identifier.
     *
     * @param orderId internal order identifier
     * @return optional containing the matching payment when found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(UUID orderId) {
        return jpaPaymentRepository
                .findByOrderId(orderId)
                .map(paymentMapper::toDomain);
    }
}
