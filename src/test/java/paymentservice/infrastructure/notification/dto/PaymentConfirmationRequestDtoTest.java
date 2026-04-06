package paymentservice.infrastructure.notification.dto;

import com.rzodeczko.paymentservice.infrastructure.notification.dto.PaymentConfirmationRequestDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentConfirmationRequestDtoTest {

    @Test
    void paymentId_shouldReturnProvidedValue() {
        UUID paymentId = UUID.randomUUID();

        PaymentConfirmationRequestDto dto = new PaymentConfirmationRequestDto(paymentId);

        assertThat(dto.paymentId()).isEqualTo(paymentId);
    }
}

