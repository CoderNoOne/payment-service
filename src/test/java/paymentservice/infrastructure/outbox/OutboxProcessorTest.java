package paymentservice.infrastructure.outbox;

import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.repository.OutboxEventRepository;
import com.rzodeczko.paymentservice.infrastructure.outbox.OutboxEventSender;
import com.rzodeczko.paymentservice.infrastructure.outbox.OutboxProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxProcessorTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OutboxEventSender outboxEventSender;

    @InjectMocks
    private OutboxProcessor outboxProcessor;

    @Test
    void process_shouldReturnImmediately_whenNoPendingEvents() {
        // given
        when(outboxEventRepository.findPending(100)).thenReturn(List.of());

        // when
        outboxProcessor.process();

        // then
        verify(outboxEventRepository).findPending(100);
        verify(outboxEventSender, never()).send(org.mockito.ArgumentMatchers.any(OutboxEvent.class));
    }

    @Test
    void process_shouldSendEachPendingEvent_whenBatchIsNotEmpty() {
        // given
        OutboxEvent firstEvent = mock(OutboxEvent.class);
        OutboxEvent secondEvent = mock(OutboxEvent.class);
        when(outboxEventRepository.findPending(100)).thenReturn(List.of(firstEvent, secondEvent));

        // when
        outboxProcessor.process();

        // then
        verify(outboxEventRepository).findPending(100);
        verify(outboxEventSender).send(firstEvent);
        verify(outboxEventSender).send(secondEvent);
        verify(outboxEventSender, times(2)).send(org.mockito.ArgumentMatchers.any(OutboxEvent.class));
    }

    @Test
    void process_shouldPropagateException_whenSenderFails() {
        // given
        OutboxEvent event = mock(OutboxEvent.class);
        when(outboxEventRepository.findPending(100)).thenReturn(List.of(event));
        doThrow(new RuntimeException("send failed")).when(outboxEventSender).send(event);

        // when / then
        assertThatThrownBy(() -> outboxProcessor.process())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("send failed");

        verify(outboxEventRepository).findPending(100);
        verify(outboxEventSender).send(event);
    }
}

