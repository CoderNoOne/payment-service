package com.rzodeczko.paymentservice.infrastructure.persistence.adapter;

import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.model.OutboxEventStatus;
import com.rzodeczko.paymentservice.domain.repository.OutboxEventRepository;
import com.rzodeczko.paymentservice.infrastructure.persistence.mapper.OutboxEventMapper;
import com.rzodeczko.paymentservice.infrastructure.persistence.repository.JpaOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Infrastructure adapter that implements the domain {@link OutboxEventRepository}
 * contract using Spring Data JPA.
 *
 * <p>This adapter maps outbox events between domain and persistence models and
 * provides status-based access for asynchronous dispatch processing.</p>
 */
@Component
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final OutboxEventMapper outboxEventMapper;

    /**
     * Persists an outbox event in the database.
     *
     * @param event outbox event to persist
     */
    @Override
    @Transactional
    public void save(OutboxEvent event) {
        jpaOutboxEventRepository.saveAndFlush(outboxEventMapper.toEntity(event));
    }

    /**
     * Retrieves all outbox events that are pending dispatch.
     *
     * @return list of pending outbox events mapped to the domain model
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
}
