package com.rzodeczko.paymentservice.application.port.input;

import java.util.UUID;

// Wyjście z use case'a. Alternatywnie spotkasz Response (częściej w REST API) lub Dto. Result
// jest czystszy domenowo — nie sugeruje transportu HTTP.
public record  InitPaymentResult(UUID paymentId, String redirectUrl) {
}
