package com.rzodeczko.paymentservice.infrastructure.gateway.tpay.adapter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Applies resilience policies dedicated to outbound TPay calls.
 */
@Component
public class TPayResilienceOperations {

    @Retry(name = "tpay-access-token")
    @CircuitBreaker(name = "tpay-access-token", fallbackMethod = "accessTokenFallback")
    public <T> T executeAccessToken(Supplier<T> accessTokenMethodSuppl, String apiCallContext) {
        return accessTokenMethodSuppl.get();
    }

    @Retry(name = "tpay-verification")
    @CircuitBreaker(name = "tpay-verification", fallbackMethod = "verificationFallback")
    public <T> T executeVerification(Supplier<T> verificationMethodSuppl, String apiCallContext) {
        return verificationMethodSuppl.get();
    }

    private <T> T accessTokenFallback(Supplier<T> ignored, String apiCallContext, Throwable t) {
        throw new IllegalStateException("TPay OAuth unavailable | " + apiCallContext, t);
    }

    private <T> T verificationFallback(Supplier<T> ignored, String apiCallContext, Throwable t) {
        throw new IllegalStateException("TPay verification unavailable | " + apiCallContext, t);
    }
}
