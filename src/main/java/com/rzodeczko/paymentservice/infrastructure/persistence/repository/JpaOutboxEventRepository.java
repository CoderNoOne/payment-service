package com.rzodeczko.paymentservice.infrastructure.persistence.repository;

import com.rzodeczko.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    List<OutboxEventEntity> findAllByStatus(String status);
}
