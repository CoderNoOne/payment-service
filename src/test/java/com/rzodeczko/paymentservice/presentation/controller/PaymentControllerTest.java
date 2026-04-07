package com.rzodeczko.paymentservice.presentation.controller;

import com.rzodeczko.paymentservice.application.port.input.InitPaymentResult;
import com.rzodeczko.paymentservice.application.port.input.NotificationCommand;
import com.rzodeczko.paymentservice.application.port.input.PaymentUseCase;
import com.rzodeczko.paymentservice.domain.exception.PaymentConcurrentModificationException;
import com.rzodeczko.paymentservice.presentation.dto.InitPaymentRequestDto;
import com.rzodeczko.paymentservice.presentation.dto.InitPaymentResponseDto;
import com.rzodeczko.paymentservice.presentation.dto.NotificationResponseDto;
import com.rzodeczko.paymentservice.presentation.dto.PaymentResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentUseCase paymentUseCase;

    @InjectMocks
    private PaymentController paymentController;

    @Captor
    private ArgumentCaptor<NotificationCommand> notificationCommandCaptor;

    @Test
    void initPayment_shouldReturnPaymentIdAndRedirectUrl() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        InitPaymentRequestDto request = new InitPaymentRequestDto(
                orderId,
                new BigDecimal("99.99"),
                "john.doe@example.com",
                "John Doe"
        );

        when(paymentUseCase.initPayment(request.orderId(), request.amount(), request.email(), request.name()))
                .thenReturn(new InitPaymentResult(paymentId, "https://tpay.com/redirect"));

        ResponseEntity<InitPaymentResponseDto> response = paymentController.initPayment(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().paymentId()).isEqualTo(paymentId);
        assertThat(response.getBody().redirectUrl()).isEqualTo("https://tpay.com/redirect");
        verify(paymentUseCase).initPayment(request.orderId(), request.amount(), request.email(), request.name());
    }

    @Test
    void handleNotification_shouldReturnTrueAndMapCommand_whenProcessingSucceeds() {
        ResponseEntity<NotificationResponseDto> response = paymentController.handleNotification(
                "merchant-1",
                "tr-123",
                "2026-04-07 12:00:00",
                "crc-abc",
                "100.00",
                "100.00",
                "TRUE",
                "john.doe@example.com",
                "none",
                "order 100",
                "md5-value"
        );

        verify(paymentUseCase).handleNotification(notificationCommandCaptor.capture());
        NotificationCommand captured = notificationCommandCaptor.getValue();
        assertThat(captured.merchantId()).isEqualTo("merchant-1");
        assertThat(captured.trId()).isEqualTo("tr-123");
        assertThat(captured.trDate()).isEqualTo("2026-04-07 12:00:00");
        assertThat(captured.trCrc()).isEqualTo("crc-abc");
        assertThat(captured.trAmount()).isEqualTo("100.00");
        assertThat(captured.trPaid()).isEqualTo("100.00");
        assertThat(captured.trStatus()).isEqualTo("TRUE");
        assertThat(captured.trEmail()).isEqualTo("john.doe@example.com");
        assertThat(captured.trError()).isEqualTo("none");
        assertThat(captured.trDesc()).isEqualTo("order 100");
        assertThat(captured.md5Sum()).isEqualTo("md5-value");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo("TRUE");
    }

    @Test
    void handleNotification_shouldReturn500_whenConcurrentModificationOccurs() {
        doThrow(new PaymentConcurrentModificationException())
                .when(paymentUseCase)
                .handleNotification(org.mockito.ArgumentMatchers.any(NotificationCommand.class));

        ResponseEntity<NotificationResponseDto> response = paymentController.handleNotification(
                "merchant-1", "tr-123", "2026-04-07 12:00:00", "crc-abc", "100.00",
                "100.00", "TRUE", "john.doe@example.com", "none", "order 100", "md5-value"
        );

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo("FALSE");
    }

    @Test
    void handleNotification_shouldReturn500_whenIllegalStateOccurs() {
        doThrow(new IllegalStateException("tpay unavailable"))
                .when(paymentUseCase)
                .handleNotification(org.mockito.ArgumentMatchers.any(NotificationCommand.class));

        ResponseEntity<NotificationResponseDto> response = paymentController.handleNotification(
                "merchant-1", "tr-123", "2026-04-07 12:00:00", "crc-abc", "100.00",
                "100.00", "TRUE", "john.doe@example.com", "none", "order 100", "md5-value"
        );

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo("FALSE");
    }

    @Test
    void handleNotification_shouldReturn400_whenUnexpectedExceptionOccurs() {
        doThrow(new RuntimeException("invalid signature"))
                .when(paymentUseCase)
                .handleNotification(org.mockito.ArgumentMatchers.any(NotificationCommand.class));

        ResponseEntity<NotificationResponseDto> response = paymentController.handleNotification(
                "merchant-1", "tr-123", "2026-04-07 12:00:00", "crc-abc", "100.00",
                "100.00", "TRUE", "john.doe@example.com", "none", "order 100", "md5-value"
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo("FALSE");
    }

    @Test
    void success_shouldReturnPaymentOkMessage() {
        ResponseEntity<PaymentResponseDto> response = paymentController.success();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("PAYMENT OK");
    }

    @Test
    void error_shouldReturnPaymentErrorMessage() {
        ResponseEntity<PaymentResponseDto> response = paymentController.error();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("PAYMENT ERROR");
    }
}


