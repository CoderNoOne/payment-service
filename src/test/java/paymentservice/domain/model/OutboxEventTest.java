package paymentservice.domain.model;

import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.model.OutboxEventStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OutboxEventTest {

    @Test
    void testCreate() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        // Act
        OutboxEvent event = OutboxEvent.create(orderId, paymentId);

        // Assert
        assertThat(event.id()).isNotNull();
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.paymentId()).isEqualTo(paymentId);
        assertThat(event.status()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.retryCount()).isZero();
        assertThat(event.createdAt()).isNotNull();
        assertThat(event.processedAt()).isNull();
    }

    @Test
    void testMarkSent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.create(orderId, paymentId);

        // Act
        event.markSent();

        // Assert
        assertThat(event.status()).isEqualTo(OutboxEventStatus.SENT);
        assertThat(event.processedAt()).isNotNull();
    }

    @Test
    void testMarkFailed_IncrementsRetryCount() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.create(orderId, paymentId);

        // Act
        event.markFailed();

        // Assert
        assertThat(event.status()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.retryCount()).isEqualTo(1);
    }

    @Test
    void testMarkFailed_MarksAsFailedAfterMaxRetries() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.create(orderId, paymentId);

        // Act
        for (int i = 0; i < 5; i++) {
            event.markFailed();
        }

        // Assert
        assertThat(event.status()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.retryCount()).isEqualTo(5);
    }

    @Test
    void testMarkFailed_DoesNotChangeStatusAfterFailed() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.create(orderId, paymentId);

        // Act
        for (int i = 0; i < 6; i++) {
            event.markFailed();
        }

        // Assert
        assertThat(event.status()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.retryCount()).isEqualTo(6); // retryCount continues to increase, but status is already FAILED
    }

    @Test
    void testConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OutboxEventStatus status = OutboxEventStatus.PENDING;
        int retryCount = 2;
        Instant createdAt = Instant.now();
        Instant processedAt = Instant.now();

        // Act
        OutboxEvent event = new OutboxEvent(id, orderId, paymentId, status, retryCount, createdAt, processedAt);

        // Assert
        assertThat(event.id()).isEqualTo(id);
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.paymentId()).isEqualTo(paymentId);
        assertThat(event.status()).isEqualTo(status);
        assertThat(event.retryCount()).isEqualTo(retryCount);
        assertThat(event.createdAt()).isEqualTo(createdAt);
        assertThat(event.processedAt()).isEqualTo(processedAt);
    }

}