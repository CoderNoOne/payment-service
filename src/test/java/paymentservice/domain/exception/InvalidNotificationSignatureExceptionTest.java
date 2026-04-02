package paymentservice.domain.exception;

import com.rzodeczko.paymentservice.domain.exception.InvalidNotificationSignatureException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvalidNotificationSignatureExceptionTest {

    @Test
    void testThrowsInvalidNotificationSignatureException() {
        // Act & Assert
        assertThatThrownBy(() -> {
            throw new InvalidNotificationSignatureException();
        })
                .isInstanceOf(InvalidNotificationSignatureException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid notification signature");
    }

    @Test
    void testExceptionMessage() {
        // Arrange
        String expectedMessage = "Invalid notification signature";

        // Act
        InvalidNotificationSignatureException exception = new InvalidNotificationSignatureException();

        // Assert
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void testExceptionIsRuntimeException() {
        // Act
        InvalidNotificationSignatureException exception = new InvalidNotificationSignatureException();

        // Assert
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

}

