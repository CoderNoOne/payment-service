package paymentservice.domain.exception;

import com.rzodeczko.paymentservice.domain.exception.PaymentAlreadyExistsException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentAlreadyExistsExceptionTest {

    @Test
    void testThrowsPaymentAlreadyExistsException() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act & Assert
        assertThatThrownBy(() -> {
            throw new PaymentAlreadyExistsException(orderId);
        })
                .isInstanceOf(PaymentAlreadyExistsException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment already exists for order id")
                .hasMessageContaining(orderId.toString());
    }

    @Test
    void testExceptionMessage() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String expectedMessage = "Payment already exists for order id " + orderId;

        // Act
        PaymentAlreadyExistsException exception = new PaymentAlreadyExistsException(orderId);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void testExceptionIsRuntimeException() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        PaymentAlreadyExistsException exception = new PaymentAlreadyExistsException(orderId);

        // Assert
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

}