package com.rzodeczko.paymentservice.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record InitPaymentRequestDto(
        @NotNull(message = "orderId cannot be null")
        UUID orderId,

        @NotNull(message = "amount cannot be null")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "email cannot be blank")
        @Email(message = "email must be valid")
        String email,

        @NotBlank(message = "name cannot be blank")
        String name
) {
}
