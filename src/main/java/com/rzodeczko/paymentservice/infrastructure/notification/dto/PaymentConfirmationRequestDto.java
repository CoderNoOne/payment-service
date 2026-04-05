package com.rzodeczko.paymentservice.infrastructure.notification.dto;

import java.util.UUID;

public record PaymentConfirmationRequestDto(UUID paymentId) {
}
