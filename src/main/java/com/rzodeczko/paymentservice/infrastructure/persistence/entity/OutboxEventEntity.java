package com.rzodeczko.paymentservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing an outbox event persisted for asynchronous dispatch.
 *
 * <p>This model maps the {@code outbox_events} table and supports reliable
 * event publication with retry metadata.</p>
 */
@Entity
@Table(
        name = "outbox_events",
        indexes = @Index(name = "idx_outbox_status", columnList = "status")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OutboxEventEntity {

    @Id
    @EqualsAndHashCode.Include
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID orderId;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID paymentId;

    /**
     * Processing status used by the outbox poller to fetch pending events.
     *
     * <p>The table-level index on this column prevents repeated full scans for
     * periodic status-based queries.</p>
     */
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant processedAt;

    /**
     * Optimistic locking version used to prevent lost updates when concurrent
     * workers process the same outbox row.
     */
    @Version
    private Long version;
}
