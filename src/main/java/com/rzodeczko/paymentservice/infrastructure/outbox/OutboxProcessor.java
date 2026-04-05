package com.rzodeczko.paymentservice.infrastructure.outbox;


import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodically processes pending events from the outbox.
 *
 * <p>This component fetches a batch of pending {@link OutboxEvent} records and forwards them
 * to {@link OutboxEventSender}. The batch size is limited by {@code OUTBOX_BATCH_SIZE}.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {
    private static final int OUTBOX_BATCH_SIZE = 100;
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventSender outboxEventSender;

    /**
     * Runs scheduled outbox processing.
     *
     * <p>Scheduling behavior:</p>
     * <ul>
     *   <li>{@code fixedDelay = 5000} starts the next run 5 seconds after the previous one finishes,</li>
     *   <li>invocations do not overlap within a single application instance.</li>
     * </ul>
     *
     * <p>Distributed lock behavior ({@link SchedulerLock}):</p>
     * <ul>
     *   <li>{@code lockAtMostFor = 30s} releases the lock automatically if an instance crashes,</li>
     *   <li>{@code lockAtLeastFor = 5s} helps prevent immediate takeover by another instance.</li>
     * </ul>
     *
     * <p>Requires a dedicated {@code shedlock} table in the database.</p>
     */
    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(
            name = "outbox_processor",
            lockAtMostFor = "30s",
            lockAtLeastFor = "5s"
    )
    public void process() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPending(OUTBOX_BATCH_SIZE);
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("OutboxProcessor: processing: {} pending event(s)", pendingEvents.size());
        for (OutboxEvent event : pendingEvents) {
            outboxEventSender.send(event);
        }

        log.info("OutboxProcessor: batch completed");
    }
}
