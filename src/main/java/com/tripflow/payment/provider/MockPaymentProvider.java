package com.tripflow.payment.provider;

import com.tripflow.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Local/demo provider — no external gateway. UI uses sandbox-confirm;
 * integration tests use the signed Tripflow webhook.
 */
public class MockPaymentProvider implements PaymentProvider {

    public static final String NAME = "MOCK";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean allowsSandboxConfirm() {
        return true;
    }

    @Override
    public CreateOrderResult createOrder(CreateOrderRequest request) {
        return new CreateOrderResult("mock_" + UUID.randomUUID());
    }

    @Override
    public Optional<CheckoutSession> checkoutSession(String providerRef, BigDecimal amount) {
        return Optional.empty();
    }

    @Override
    public boolean verifyCheckoutSignature(String orderId, String paymentId, String signature) {
        return orderId != null && orderId.startsWith("mock_");
    }

    @Override
    public Optional<WebhookEvent> parseWebhook(String rawBody, String signatureHeader) {
        // Mock uses the typed Tripflow webhook endpoint, not raw provider payloads.
        return Optional.empty();
    }
}
