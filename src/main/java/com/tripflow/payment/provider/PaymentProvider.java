package com.tripflow.payment.provider;

import com.tripflow.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Abstraction over a payment gateway. Razorpay
 * {@code tripflow.payment.provider} without changing PaymentService booking rules.
 */
public interface PaymentProvider {

    String getName();

    /** When true, authenticated sandbox-confirm is allowed (local/demo only). */
    boolean allowsSandboxConfirm();

    CreateOrderResult createOrder(CreateOrderRequest request);

    /**
     * Checkout fields for the browser SDK. Empty for providers that do not open a hosted checkout.
     */
    Optional<CheckoutSession> checkoutSession(String providerRef, BigDecimal amount);

    /**
     * Verifies client-returned payment signature after Checkout success.
     * Mock always returns true for the given order id.
     */
    boolean verifyCheckoutSignature(String orderId, String paymentId, String signature);

    /**
     * Parses a provider webhook body. Empty = ignore (unknown event / not for us).
     */
    Optional<WebhookEvent> parseWebhook(String rawBody, String signatureHeader);

    record CreateOrderRequest(Long bookingId, BigDecimal amountInr, String receipt) {
    }

    record CreateOrderResult(String providerRef) {
    }

    record CheckoutSession(String keyId, long amountPaise, String currency, String orderId) {
    }

    record WebhookEvent(String providerRef, PaymentStatus status) {
    }
}
