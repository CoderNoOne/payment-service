package com.rzodeczko.paymentservice.domain.repository;


import com.rzodeczko.paymentservice.domain.model.OutboxEvent;

import java.util.List;

public interface OutboxEventRepository {
    void save(OutboxEvent event);

    List<OutboxEvent> findAllPending();
}
