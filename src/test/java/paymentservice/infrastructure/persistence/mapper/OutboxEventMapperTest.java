package paymentservice.infrastructure.persistence.mapper;

import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.model.OutboxEventStatus;
import com.rzodeczko.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.rzodeczko.paymentservice.infrastructure.persistence.mapper.OutboxEventMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventMapperTest {

    private final OutboxEventMapper mapper = new OutboxEventMapper();

    // --- toEntity ---

    @Test
    void toEntity_shouldMapAllFields() {
        // given
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant processedAt = Instant.now();

        OutboxEvent domain = new OutboxEvent(
                id, orderId, paymentId,
                OutboxEventStatus.SENT,
                3,
                createdAt, processedAt
        );

        // when
        OutboxEventEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getOrderId()).isEqualTo(orderId);
        assertThat(entity.getPaymentId()).isEqualTo(paymentId);
        assertThat(entity.getStatus()).isEqualTo("SENT");
        assertThat(entity.getRetryCount()).isEqualTo(3);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getProcessedAt()).isEqualTo(processedAt);
    }

    @Test
    void toEntity_shouldSerializeStatusAsEnumName() {
        // given
        OutboxEvent pending = new OutboxEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                OutboxEventStatus.PENDING, 0, Instant.now(), null
        );
        OutboxEvent failed = new OutboxEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                OutboxEventStatus.FAILED, 5, Instant.now(), null
        );

        // when & then
        assertThat(mapper.toEntity(pending).getStatus()).isEqualTo("PENDING");
        assertThat(mapper.toEntity(failed).getStatus()).isEqualTo("FAILED");
    }

    @Test
    void toEntity_shouldMapNullProcessedAt() {
        // given
        OutboxEvent domain = new OutboxEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                OutboxEventStatus.PENDING, 0, Instant.now(), null
        );

        // when
        OutboxEventEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getProcessedAt()).isNull();
    }

    // --- toDomain ---

    @Test
    void toDomain_shouldMapAllFields() {
        // given
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant processedAt = Instant.now();

        OutboxEventEntity entity = OutboxEventEntity.builder()
                .id(id)
                .orderId(orderId)
                .paymentId(paymentId)
                .status("SENT")
                .retryCount(2)
                .createdAt(createdAt)
                .processedAt(processedAt)
                .build();

        // when
        OutboxEvent domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getOrderId()).isEqualTo(orderId);
        assertThat(domain.getPaymentId()).isEqualTo(paymentId);
        assertThat(domain.getStatus()).isEqualTo(OutboxEventStatus.SENT);
        assertThat(domain.getRetryCount()).isEqualTo(2);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getProcessedAt()).isEqualTo(processedAt);
    }

    @Test
    void toDomain_shouldDeserializeStatusFromString() {
        // given
        OutboxEventEntity pending = OutboxEventEntity.builder()
                .id(UUID.randomUUID()).orderId(UUID.randomUUID()).paymentId(UUID.randomUUID())
                .status("PENDING").retryCount(0).createdAt(Instant.now()).build();

        OutboxEventEntity failed = OutboxEventEntity.builder()
                .id(UUID.randomUUID()).orderId(UUID.randomUUID()).paymentId(UUID.randomUUID())
                .status("FAILED").retryCount(5).createdAt(Instant.now()).build();

        // when & then
        assertThat(mapper.toDomain(pending).getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(mapper.toDomain(failed).getStatus()).isEqualTo(OutboxEventStatus.FAILED);
    }

    @Test
    void toDomain_shouldMapNullProcessedAt() {
        // given
        OutboxEventEntity entity = OutboxEventEntity.builder()
                .id(UUID.randomUUID()).orderId(UUID.randomUUID()).paymentId(UUID.randomUUID())
                .status("PENDING").retryCount(0).createdAt(Instant.now())
                .processedAt(null)
                .build();

        // when
        OutboxEvent domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getProcessedAt()).isNull();
    }

    // --- round-trip ---

    @Test
    void toEntity_andToDomain_shouldProduceEquivalentObject() {
        // given
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        OutboxEvent original = new OutboxEvent(
                id, orderId, paymentId,
                OutboxEventStatus.PENDING, 1, createdAt, null
        );

        // when
        OutboxEvent roundTripped = mapper.toDomain(mapper.toEntity(original));

        // then
        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getOrderId()).isEqualTo(original.getOrderId());
        assertThat(roundTripped.getPaymentId()).isEqualTo(original.getPaymentId());
        assertThat(roundTripped.getStatus()).isEqualTo(original.getStatus());
        assertThat(roundTripped.getRetryCount()).isEqualTo(original.getRetryCount());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(roundTripped.getProcessedAt()).isNull();
    }
}

