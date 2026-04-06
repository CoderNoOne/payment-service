package com.rzodeczko.paymentservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class PaymentServiceApplicationTest {

    @Test
    void constructor_shouldBeCallable() {
        PaymentServiceApplication application = new PaymentServiceApplication();

        org.assertj.core.api.Assertions.assertThat(application).isNotNull();
    }

    @Test
    void main_shouldDelegateToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            PaymentServiceApplication.main();

            springApplication.verify(() -> SpringApplication.run(PaymentServiceApplication.class, new String[]{}));
        }
    }
}

