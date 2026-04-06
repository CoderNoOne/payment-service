package paymentservice.infrastructure.transaction;

import com.rzodeczko.paymentservice.application.port.input.InitPaymentResult;
import com.rzodeczko.paymentservice.application.port.input.NotificationCommand;
import com.rzodeczko.paymentservice.application.service.PaymentService;
import com.rzodeczko.paymentservice.domain.exception.InvalidNotificationSignatureException;
import com.rzodeczko.paymentservice.domain.exception.PaymentAlreadyExistsException;
import com.rzodeczko.paymentservice.domain.exception.PaymentNotFoundException;
import com.rzodeczko.paymentservice.infrastructure.transaction.TransactionalPaymentUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionalPaymentUseCaseTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private TransactionalPaymentUseCase transactionalPaymentUseCase;

    // -------------------------------------------------------------------------
    // initPayment
    // -------------------------------------------------------------------------

    @Test
    void initPayment_shouldDelegateToPaymentService_andReturnResult() {
        // given
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(150.00);
        String email = "user@example.com";
        String name = "John Doe";
        UUID paymentId = UUID.randomUUID();
        String redirectUrl = "https://gateway.com/pay";
        InitPaymentResult expected = new InitPaymentResult(paymentId, redirectUrl);

        when(paymentService.initPayment(orderId, amount, email, name)).thenReturn(expected);

        // when
        InitPaymentResult result = transactionalPaymentUseCase.initPayment(orderId, amount, email, name);

        // then
        assertThat(result.paymentId()).isEqualTo(paymentId);
        assertThat(result.redirectUrl()).isEqualTo(redirectUrl);
        verify(paymentService).initPayment(orderId, amount, email, name);
    }

    @Test
    void initPayment_shouldPropagatePaymentAlreadyExistsException_whenServiceThrows() {
        // given
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String email = "user@example.com";
        String name = "John Doe";

        when(paymentService.initPayment(orderId, amount, email, name))
                .thenThrow(new PaymentAlreadyExistsException(orderId));

        // when & then
        assertThatThrownBy(() -> transactionalPaymentUseCase.initPayment(orderId, amount, email, name))
                .isInstanceOf(PaymentAlreadyExistsException.class);

        verify(paymentService).initPayment(orderId, amount, email, name);
    }

    // -------------------------------------------------------------------------
    // handleNotification
    // -------------------------------------------------------------------------

    @Test
    void handleNotification_shouldDelegateToPaymentService() {
        // given
        NotificationCommand notification = new NotificationCommand(
                "merchant", "tr123", "date", "crc",
                "100", "100", "desc", "TRUE", null, "email", "md5"
        );

        // when
        transactionalPaymentUseCase.handleNotification(notification);

        // then
        verify(paymentService).handleNotification(notification);
    }

    @Test
    void handleNotification_shouldPropagatePaymentNotFoundException_whenServiceThrows() {
        // given
        NotificationCommand notification = new NotificationCommand(
                "merchant", "tr-not-found", "date", "crc",
                "100", "100", "desc", "TRUE", null, "email", "md5"
        );

        doThrow(new PaymentNotFoundException("tr-not-found"))
                .when(paymentService).handleNotification(notification);

        // when & then
        assertThatThrownBy(() -> transactionalPaymentUseCase.handleNotification(notification))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("tr-not-found");

        verify(paymentService).handleNotification(notification);
    }

    @Test
    void handleNotification_shouldPropagateInvalidNotificationSignatureException_whenServiceThrows() {
        // given
        NotificationCommand notification = new NotificationCommand(
                "merchant", "tr123", "date", "crc",
                "100", "100", "desc", "TRUE", null, "email", "bad-md5"
        );

        doThrow(new InvalidNotificationSignatureException())
                .when(paymentService).handleNotification(notification);

        // when & then
        assertThatThrownBy(() -> transactionalPaymentUseCase.handleNotification(notification))
                .isInstanceOf(InvalidNotificationSignatureException.class);

        verify(paymentService).handleNotification(notification);
    }
}

