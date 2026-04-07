package com.rzodeczko.paymentservice.presentation.controller;

import com.rzodeczko.paymentservice.presentation.dto.HealthCheckResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/")
    public ResponseEntity<HealthCheckResponseDto> healthCheck() {
        return ResponseEntity.ok(new HealthCheckResponseDto("PAYMENT SERVICE OK"));
    }
}
