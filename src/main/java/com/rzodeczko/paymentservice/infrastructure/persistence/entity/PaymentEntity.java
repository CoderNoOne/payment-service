package com.rzodeczko.paymentservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a persisted payment record.
 *
 * <p>This model is used by the infrastructure layer to map the payments table
 * and enforce persistence-level integrity constraints.</p>
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PaymentEntity {
    @Id
    @EqualsAndHashCode.Include
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    /**
     * Enforces a one-to-one relation between order and payment at the database level.
     *
     * <p>The unique constraint protects against duplicate payment creation under
     * concurrent requests for the same order.</p>
     */
    @Column(unique = true, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID orderId;

    @Column(unique = true, nullable = false)
    private String externalTransactionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status;

    @Column(length = 512)
    private String redirectUrl;

    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Optimistic locking version used to prevent lost updates during concurrent
     * notification retries for the same payment record.
     */
    @Version
    private Long version;

}
