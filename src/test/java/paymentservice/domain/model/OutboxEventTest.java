package paymentservice.domain.model;

import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.model.OutboxEventStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    @Test
    void testCreate() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        // Act
        OutboxEvent event = OutboxEvent.create(orderId, paymentId);

        // Assert
        assertThat(event.getId()).isNotNull();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getPaymentId()).isEqualTo(paymentId);
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.getRetryCount()).isZero();
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getProcessedAt()).isNull();
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
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.SENT);
        assertThat(event.getProcessedAt()).isNotNull();
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
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.getRetryCount()).isEqualTo(1);
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
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(5);
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
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(6); // retryCount continues to increase, but status is already FAILED
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
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getPaymentId()).isEqualTo(paymentId);
        assertThat(event.getStatus()).isEqualTo(status);
        assertThat(event.getRetryCount()).isEqualTo(retryCount);
        assertThat(event.getCreatedAt()).isEqualTo(createdAt);
        assertThat(event.getProcessedAt()).isEqualTo(processedAt);
    }

}