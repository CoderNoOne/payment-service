package com.rzodeczko.paymentservice.presentation.controller;


import com.rzodeczko.paymentservice.application.port.input.InitPaymentResult;
import com.rzodeczko.paymentservice.application.port.input.NotificationCommand;
import com.rzodeczko.paymentservice.application.port.input.PaymentUseCase;
import com.rzodeczko.paymentservice.domain.exception.PaymentConcurrentModificationException;
import com.rzodeczko.paymentservice.presentation.dto.InitPaymentRequestDto;
import com.rzodeczko.paymentservice.presentation.dto.InitPaymentResponseDto;
import com.rzodeczko.paymentservice.presentation.dto.NotificationResponseDto;
import com.rzodeczko.paymentservice.presentation.dto.PaymentResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentUseCase paymentUseCase;

    @PostMapping("/init")
    public ResponseEntity<InitPaymentResponseDto> initPayment(@Valid @RequestBody InitPaymentRequestDto request) {
        InitPaymentResult result = paymentUseCase
                .initPayment(
                        request.orderId(),
                        request.amount(),
                        request.email(),
                        request.name()
                );
        return ResponseEntity.ok(new InitPaymentResponseDto(result.paymentId(), result.redirectUrl()));
    }

    @PostMapping(value = "/notification", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<NotificationResponseDto> handleNotification(
            @RequestParam("id") String merchantId,
            @RequestParam("tr_id") String trId,
            @RequestParam("tr_date") String trDate,
            @RequestParam("tr_crc") String trCrc,
            @RequestParam("tr_amount") String trAmount,
            @RequestParam("tr_paid") String trPaid,
            @RequestParam("tr_status") String trStatus,
            @RequestParam("tr_email") String trEmail,
            @RequestParam("tr_error") String trError,
            @RequestParam("tr_desc") String trDesc,
            @RequestParam("md5sum") String md5Sum
    ) {
        try {
            NotificationCommand command = new NotificationCommand(
                    merchantId, trId, trDate, trCrc, trAmount, trPaid, trDesc, trStatus, trError, trEmail, md5Sum);
            paymentUseCase.handleNotification(command);
            return ResponseEntity.ok(new NotificationResponseDto("TRUE"));
        } catch (PaymentConcurrentModificationException e) {
            // Dwa rownolegle retry od TPay - jedno wygralo optimistic lock
            // 500 - TPay ponowi - idempotency check (isPaid()) zatrzyma drugie
            log.warn("Concurrent notification, returning 500 for TPay retry. trId={}", trId);
            return ResponseEntity.internalServerError().body(new NotificationResponseDto("FALSE"));
        } catch (IllegalStateException e) {
            // TPay 5xx lub zewnetrzny serwis niedostepny -> TPay ponowi
            log.error("Notification processing failed. trId={}, reason={}", trId, e.getMessage());
            return ResponseEntity.internalServerError().body(new NotificationResponseDto("FALSE"));
        } catch (Exception e) {
            // Nieprawidlowy podpis, nieznana platnosc - 400, TPay nie ponawia
            log.error("Notification rejected. trId={}, reason={}", trId, e.getMessage());
            return ResponseEntity.badRequest().body(new NotificationResponseDto("FALSE"));
        }
    }

    @GetMapping("/success")
    public ResponseEntity<PaymentResponseDto> success() {
        return ResponseEntity.ok(new PaymentResponseDto("PAYMENT OK"));
    }

    @GetMapping("/error")
    public ResponseEntity<PaymentResponseDto> error() {
        return ResponseEntity.ok(new PaymentResponseDto("PAYMENT ERROR"));
    }
}
