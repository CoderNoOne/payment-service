package com.rzodeczko.paymentservice.presentation.dto;

import java.util.UUID;

public record InitPaymentResponseDto(UUID paymentId, String redirectUrl) {
}
