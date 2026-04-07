package paymentservice.domain.exception;

import com.rzodeczko.paymentservice.domain.exception.PaymentConcurrentModificationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentConcurrentModificationExceptionTest {

    @Test
    void testThrowsPaymentConcurrentModificationException() {
        // Act & Assert
        assertThatThrownBy(() -> {
            throw new PaymentConcurrentModificationException();
        })
                .isInstanceOf(PaymentConcurrentModificationException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Payment was modified concurrently, please retry");
    }

    @Test
    void testExceptionMessage() {
        // Arrange
        String expectedMessage = "Payment was modified concurrently, please retry";

        // Act
        PaymentConcurrentModificationException exception = new PaymentConcurrentModificationException();

        // Assert
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void testExceptionIsRuntimeException() {
        // Act
        PaymentConcurrentModificationException exception = new PaymentConcurrentModificationException();

        // Assert
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}

