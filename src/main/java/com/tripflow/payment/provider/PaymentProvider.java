package com.tripflow.payment.provider;

import java.math.BigDecimal;

/**
 * Abstraction over a payment gateway. V1 uses MockPaymentProvider;
 * swap in Razorpay/Stripe later without changing PaymentService.
 */
public interface PaymentProvider {

    String getName();

    ChargeResult charge(ChargeRequest request);

    record ChargeRequest(Long bookingId, BigDecimal amount, String providerRef) {
    }

    record ChargeResult(boolean success, String providerRef) {
    }
}
