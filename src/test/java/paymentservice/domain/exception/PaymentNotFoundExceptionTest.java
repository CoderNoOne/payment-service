package paymentservice.domain.exception;

import com.rzodeczko.paymentservice.domain.exception.PaymentNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentNotFoundExceptionTest {

    @Test
    void testThrowsPaymentNotFoundExceptionWithExternalTransactionId() {
        // Arrange
        String externalTransactionId = "tr_12345";

        // Act & Assert
        assertThatThrownBy(() -> {
            throw new PaymentNotFoundException(externalTransactionId);
        })
                .isInstanceOf(PaymentNotFoundException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found for external transaction id")
                .hasMessageContaining(externalTransactionId);
    }

    @Test
    void testThrowsPaymentNotFoundExceptionWithOrderId() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act & Assert
        assertThatThrownBy(() -> {
            throw new PaymentNotFoundException(orderId);
        })
                .isInstanceOf(PaymentNotFoundException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found for order id")
                .hasMessageContaining(orderId.toString());
    }

    @Test
    void testExceptionMessageWithExternalTransactionId() {
        // Arrange
        String externalTransactionId = "tr_12345";
        String expectedMessage = "Payment not found for external transaction id " + externalTransactionId;

        // Act
        PaymentNotFoundException exception = new PaymentNotFoundException(externalTransactionId);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void testExceptionMessageWithOrderId() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String expectedMessage = "Payment not found for order id " + orderId;

        // Act
        PaymentNotFoundException exception = new PaymentNotFoundException(orderId);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void testExceptionIsRuntimeException() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        PaymentNotFoundException exception = new PaymentNotFoundException(orderId);

        // Assert
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

}

