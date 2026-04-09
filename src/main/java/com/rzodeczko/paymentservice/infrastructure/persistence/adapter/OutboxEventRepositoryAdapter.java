package com.rzodeczko.paymentservice.infrastructure.persistence.adapter;

import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.model.OutboxEventStatus;
import com.rzodeczko.paymentservice.domain.repository.OutboxEventRepository;
import com.rzodeczko.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.rzodeczko.paymentservice.infrastructure.persistence.mapper.OutboxEventMapper;
import com.rzodeczko.paymentservice.infrastructure.persistence.repository.JpaOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Infrastructure adapter implementing the domain {@link OutboxEventRepository}
 * contract with Spring Data JPA.
 *
 * <p>The adapter maps outbox events between domain and persistence models and
 * exposes status-based queries used by asynchronous dispatch processing.</p>
 */
@Component
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final OutboxEventMapper outboxEventMapper;

    /**
     * Saves an outbox event.
     *
     * <p>If an event with the same identifier already exists, its mutable fields
     * are updated (status, retry count and processed timestamp). Otherwise, a new
     * record is created.</p>
     *
     * @param event outbox event to create or update
     */
    @Override
    @Transactional
    public void save(OutboxEvent event) {
        jpaOutboxEventRepository.findById(event.getId())
                .map(existing -> {
                    existing.setStatus(event.getStatus().name());
                    existing.setRetryCount(event.getRetryCount());
                    existing.setProcessedAt(event.getProcessedAt());
                    return jpaOutboxEventRepository.saveAndFlush(existing);
                })
                .orElseGet(() -> {
                    OutboxEventEntity newEntity = outboxEventMapper.toEntity(event);
                    return jpaOutboxEventRepository.saveAndFlush(newEntity);
                });
    }

    /**
     * Retrieves all outbox events marked as pending dispatch.
     *
     * @return pending outbox events mapped to the domain model
     */
    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findAllPending() {
        return jpaOutboxEventRepository
                .findAllByStatus(OutboxEventStatus.PENDING.name())
                .stream()
                .map(outboxEventMapper::toDomain)
                .toList();
    }

    /**
     * Retrieves at most {@code limit} pending outbox events ordered by creation time.
     *
     * <p>The oldest events are returned first ({@code createdAt} ascending) to preserve
     * FIFO-like processing semantics in the outbox dispatcher.</p>
     *
     * @param limit maximum number of pending events to fetch
     * @return pending outbox events mapped to the domain model
     */
    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findPending(int limit) {
        PageRequest pageRequest = PageRequest.of(
                0,
                limit,
                Sort.by("createdAt").ascending());
        return jpaOutboxEventRepository.findByStatus(OutboxEventStatus.PENDING.name(), pageRequest)
                .stream()
                .map(outboxEventMapper::toDomain)
                .toList();
    }
}
