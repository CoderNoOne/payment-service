package paymentservice.infrastructure.outbox;

import com.rzodeczko.paymentservice.application.port.output.NotificationPort;
import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.model.OutboxEventStatus;
import com.rzodeczko.paymentservice.domain.repository.OutboxEventRepository;
import com.rzodeczko.paymentservice.infrastructure.outbox.OutboxEventSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventSenderTest {

    @Mock
    private NotificationPort notificationPort;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OutboxEventSender outboxEventSender;

    @Test
    void send_shouldMarkEventAsSentAndPersist_whenNotificationSucceeds() {
        // given
        OutboxEvent event = buildPendingEvent(0);

        // when
        outboxEventSender.send(event);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.SENT);
        assertThat(event.getProcessedAt()).isNotNull();
        verify(notificationPort).notifyExternalService(event.getOrderId(), event.getPaymentId());
        verify(outboxEventRepository).save(event);
    }

    @Test
    void send_shouldKeepEventPendingAndIncrementRetry_whenNotificationFailsBeforeRetryLimit() {
        // given
        OutboxEvent event = buildPendingEvent(0);
        doThrow(new RuntimeException("network error"))
                .when(notificationPort)
                .notifyExternalService(event.getOrderId(), event.getPaymentId());

        // when
        outboxEventSender.send(event);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getProcessedAt()).isNull();
        verify(notificationPort).notifyExternalService(event.getOrderId(), event.getPaymentId());
        verify(outboxEventRepository).save(event);
    }

    @Test
    void send_shouldMarkEventFailed_whenNotificationFailsAtRetryLimit() {
        // given
        OutboxEvent event = buildPendingEvent(4);
        doThrow(new RuntimeException("gateway timeout"))
                .when(notificationPort)
                .notifyExternalService(event.getOrderId(), event.getPaymentId());

        // when
        outboxEventSender.send(event);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(5);
        verify(notificationPort).notifyExternalService(event.getOrderId(), event.getPaymentId());
        verify(outboxEventRepository).save(event);
    }

    private OutboxEvent buildPendingEvent(int retryCount) {
        return new OutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OutboxEventStatus.PENDING,
                retryCount,
                Instant.now(),
                null
        );
    }
}

