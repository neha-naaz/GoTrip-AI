package com.tripflow.payment.controller;

import com.tripflow.payment.config.PaymentProperties;
import com.tripflow.payment.dto.PaymentWebhookRequest;
import com.tripflow.payment.exception.WebhookUnauthorizedException;
import com.tripflow.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    public static final String WEBHOOK_SECRET_HEADER = "X-Tripflow-Webhook-Secret";

    private final PaymentService paymentService;
    private final PaymentProperties paymentProperties;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = WEBHOOK_SECRET_HEADER, required = false) String secret,
            @Valid @RequestBody PaymentWebhookRequest request) {
        if (secret == null || !secret.equals(paymentProperties.getWebhookSecret())) {
            throw new WebhookUnauthorizedException();
        }
        paymentService.handleWebhook(request.getProviderRef(), request.getStatus());
        return ResponseEntity.ok().build();
    }
}
