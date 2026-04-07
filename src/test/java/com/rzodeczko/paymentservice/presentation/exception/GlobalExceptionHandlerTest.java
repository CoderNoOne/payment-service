package com.rzodeczko.paymentservice.presentation.exception;

import com.rzodeczko.paymentservice.domain.exception.InvalidNotificationSignatureException;
import com.rzodeczko.paymentservice.domain.exception.PaymentAlreadyExistsException;
import com.rzodeczko.paymentservice.domain.exception.PaymentConcurrentModificationException;
import com.rzodeczko.paymentservice.domain.exception.PaymentNotFoundException;
import com.rzodeczko.paymentservice.presentation.dto.ErrorResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void handlePaymentNotFoundException_shouldReturn404() {
        PaymentNotFoundException exception = new PaymentNotFoundException("ext-123");

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handle(exception);

        assertErrorResponse(response, 404, "Not Found", "Payment not found for external transaction id ext-123");
    }

    @Test
    void handleInvalidNotificationSignatureException_shouldReturn400() {
        InvalidNotificationSignatureException exception = new InvalidNotificationSignatureException();

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handle(exception);

        assertErrorResponse(response, 400, "Bad Request", "Invalid notification signature");
    }

    @Test
    void handlePaymentAlreadyExistsException_shouldReturn409() {
        UUID orderId = UUID.randomUUID();
        PaymentAlreadyExistsException exception = new PaymentAlreadyExistsException(orderId);

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handle(exception);

        assertErrorResponse(response, 409, "Conflict", "Payment already exists for order id " + orderId);
    }

    @Test
    void handlePaymentConcurrentModificationException_shouldReturn409() {
        PaymentConcurrentModificationException exception = new PaymentConcurrentModificationException();

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handle(exception);

        assertErrorResponse(response, 409, "Conflict", "Payment was modified concurrently, please retry");
    }

    @Test
    void handleMethodArgumentNotValidException_shouldReturn400WithCombinedFieldErrors() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("initPaymentRequestDto", "email", "email must be valid"),
                new FieldError("initPaymentRequestDto", "amount", "amount must be greater than 0")
        ));

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handle(methodArgumentNotValidException);

        assertErrorResponse(
                response,
                400,
                "Validation failed",
                "email: email must be valid, amount: amount must be greater than 0"
        );
    }

    @Test
    void handleIllegalStateException_shouldReturn500() {
        IllegalStateException exception = new IllegalStateException("gateway unavailable");

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handle(exception);

        assertErrorResponse(response, 500, "Internal Server Error", "gateway unavailable");
    }

    @Test
    void handleException_shouldReturn500WithGenericMessage() {
        Exception exception = new Exception("sensitive details");

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handle(exception);

        assertErrorResponse(response, 500, "Internal Server Error", "Unexpected error");
    }

    private void assertErrorResponse(
            ResponseEntity<ErrorResponseDto> response,
            int expectedStatus,
            String expectedError,
            String expectedMessage
    ) {
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(expectedStatus);
        assertThat(response.getBody().error()).isEqualTo(expectedError);
        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}

