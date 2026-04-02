package paymentservice.application.service;

import com.rzodeczko.paymentservice.application.port.input.InitPaymentResult;
import com.rzodeczko.paymentservice.application.port.input.NotificationCommand;
import com.rzodeczko.paymentservice.application.port.output.GatewayResult;
import com.rzodeczko.paymentservice.application.port.output.PaymentGatewayPort;
import com.rzodeczko.paymentservice.application.service.PaymentService;
import com.rzodeczko.paymentservice.domain.exception.InvalidNotificationSignatureException;
import com.rzodeczko.paymentservice.domain.exception.PaymentNotFoundException;
import com.rzodeczko.paymentservice.domain.model.OutboxEvent;
import com.rzodeczko.paymentservice.domain.model.Payment;
import com.rzodeczko.paymentservice.domain.model.PaymentStatus;
import com.rzodeczko.paymentservice.domain.repository.OutboxEventRepository;
import com.rzodeczko.paymentservice.domain.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayPort paymentGatewayPort;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void initPayment_shouldReturnExistingPayment_whenPaymentAlreadyExists() {
        // Given
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String email = "test@example.com";
        String name = "Test User";
        UUID paymentId = UUID.randomUUID();
        String redirectUrl = "https://gateway.com/pay";
        Payment existingPayment = new Payment(paymentId, orderId, amount, PaymentStatus.PENDING, "ext123", redirectUrl, java.time.Instant.now());

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingPayment));

        // When
        InitPaymentResult result = paymentService.initPayment(orderId, amount, email, name);

        // Then
        assertThat(result.paymentId()).isEqualTo(paymentId);
        assertThat(result.redirectUrl()).isEqualTo(redirectUrl);
        verify(paymentRepository, never()).save(any());
        verify(paymentGatewayPort, never()).registerTransaction(any(), any(), any(), any());
    }

    @Test
    void initPayment_shouldCreateNewPayment_whenPaymentDoesNotExist() {
        // Given
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String email = "test@example.com";
        String name = "Test User";
        String externalTransactionId = "ext123";
        String redirectUrl = "https://gateway.com/pay";
        GatewayResult gatewayResult = new GatewayResult(redirectUrl, externalTransactionId);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentGatewayPort.registerTransaction(orderId, amount, email, name)).thenReturn(gatewayResult);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        InitPaymentResult result = paymentService.initPayment(orderId, amount, email, name);

        // Then
        assertThat(result.redirectUrl()).isEqualTo(redirectUrl);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentGatewayPort).registerTransaction(orderId, amount, email, name);
    }

    @Test
    void handleNotification_shouldThrowInvalidNotificationSignatureException_whenSignatureInvalid() {
        // Given
        NotificationCommand notification = new NotificationCommand("merchant", "tr123", "date", "crc", "100", "100", "desc", "TRUE", null, "email", "md5");
        when(paymentGatewayPort.verifyNotificationSignature(notification)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> paymentService.handleNotification(notification))
                .isInstanceOf(InvalidNotificationSignatureException.class);
        verify(paymentRepository, never()).findByExternalTransactionId(any());
    }

    @Test
    void handleNotification_shouldThrowPaymentNotFoundException_whenPaymentNotFound() {
        // Given
        NotificationCommand notification = new NotificationCommand("merchant", "tr123", "date", "crc", "100", "100", "desc", "TRUE", null, "email", "md5");

        when(paymentGatewayPort.verifyNotificationSignature(notification)).thenReturn(true);
        when(paymentRepository.findByExternalTransactionId("tr123")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.handleNotification(notification))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("tr123");
    }

    @Test
    void handleNotification_shouldDoNothing_whenPaymentAlreadyPaid() {
        // Given
        NotificationCommand notification = new NotificationCommand("merchant", "tr123", "date", "crc", "100", "100", "desc", "True", null, "email", "md5");
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(paymentId, orderId, BigDecimal.valueOf(100), PaymentStatus.PAID, "tr123", "url", java.time.Instant.now());

        when(paymentGatewayPort.verifyNotificationSignature(notification)).thenReturn(true);
        when(paymentRepository.findByExternalTransactionId("tr123")).thenReturn(Optional.of(payment));

        // When
        paymentService.handleNotification(notification);

        // Then
        verify(paymentRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
        verify(paymentGatewayPort, never()).verifyTransactionConfirmed(any());
    }

    @Test
    void handleNotification_shouldConfirmPayment_whenStatusTrueAndConfirmed() {
        // Given
        NotificationCommand notification = new NotificationCommand("merchant", "tr123", "date", "crc", "100", "100", "desc", "TRUE", null, "email", "md5");
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(paymentId, orderId, BigDecimal.valueOf(100), PaymentStatus.PENDING, "tr123", "url", java.time.Instant.now());

        when(paymentGatewayPort.verifyNotificationSignature(notification)).thenReturn(true);
        when(paymentRepository.findByExternalTransactionId("tr123")).thenReturn(Optional.of(payment));
        when(paymentGatewayPort.verifyTransactionConfirmed("tr123")).thenReturn(true);

        // When
        paymentService.handleNotification(notification);

        // Then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(paymentRepository).save(payment);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void handleNotification_shouldDoNothing_whenStatusTrueButNotConfirmed() {
        // Given
        NotificationCommand notification = new NotificationCommand("merchant", "tr123", "date", "crc", "100", "100", "desc", "TRUE", null, "email", "md5");
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(paymentId, orderId, BigDecimal.valueOf(100), PaymentStatus.PENDING, "tr123", "url", java.time.Instant.now());

        when(paymentGatewayPort.verifyNotificationSignature(notification)).thenReturn(true);
        when(paymentRepository.findByExternalTransactionId("tr123")).thenReturn(Optional.of(payment));
        when(paymentGatewayPort.verifyTransactionConfirmed("tr123")).thenReturn(false);

        // When
        paymentService.handleNotification(notification);

        // Then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void handleNotification_shouldFailPayment_whenStatusNotTrue() {
        // Given
        NotificationCommand notification = new NotificationCommand("merchant", "tr123", "date", "crc", "100", "100", "desc", "FALSE", "error", "email", "md5");
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(paymentId, orderId, BigDecimal.valueOf(100), PaymentStatus.PENDING, "tr123", "url", java.time.Instant.now());

        when(paymentGatewayPort.verifyNotificationSignature(notification)).thenReturn(true);
        when(paymentRepository.findByExternalTransactionId("tr123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        paymentService.handleNotification(notification);

        // Then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentRepository).save(payment);
        verify(outboxEventRepository, never()).save(any());
        verify(paymentGatewayPort, never()).verifyTransactionConfirmed(any());
    }
}
