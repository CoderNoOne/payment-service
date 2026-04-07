package com.rzodeczko.paymentservice.presentation.dto;

/**
 * Response payload returned to TPay notification callbacks.
 *
 * @param result notification processing result expected by TPay (for example: TRUE/FALSE)
 */
public record NotificationResponseDto(String result) {
}
