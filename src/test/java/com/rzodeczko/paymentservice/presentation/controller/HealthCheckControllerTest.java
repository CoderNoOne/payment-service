package com.rzodeczko.paymentservice.presentation.controller;

import com.rzodeczko.paymentservice.presentation.dto.HealthCheckResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class HealthCheckControllerTest {

    private final HealthCheckController healthCheckController = new HealthCheckController();

    @Test
    void healthCheck_shouldReturnHttp200AndExpectedMessage() {
        ResponseEntity<HealthCheckResponseDto> response = healthCheckController.healthCheck();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("PAYMENT SERVICE OK");
    }
}

