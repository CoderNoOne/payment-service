package com.rzodeczko.paymentservice.application.port.input;

import java.math.BigDecimal;
import java.util.UUID;

// Input port = "przypadek użycia". Interfejs opisuje co aplikacja potrafi zrobić
// z perspektywy świata zewnętrznego. Nazewnictwo celowo nie mówi "serwis" czy
// "manager" — mówi o intencji biznesowej.

// Stosujac odpowiednie nazwy otrzymujesz flow:
// Controller -> Command -> UseCase -> Result -> Controller
public interface PaymentUseCase {
    InitPaymentResult initPayment(UUID orderId, BigDecimal amount, String email, String name);
    void handleNotification(NotificationCommand notification);
}
