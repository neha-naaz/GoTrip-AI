package com.tripflow.payment.dto;

import com.tripflow.payment.entity.Payment;
import com.tripflow.payment.entity.PaymentStatus;
import com.tripflow.payment.provider.PaymentProvider;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResponse {

    private final Long id;
    private final Long bookingId;
    private final BigDecimal amount;
    private final PaymentStatus status;
    private final String provider;
    private final String providerRef;
    /** Present when the browser should open hosted checkout (e.g. Razorpay). */
    private final String checkoutKeyId;
    private final Long amountPaise;
    private final String currency;

    public static PaymentResponse from(Payment payment) {
        return from(payment, null);
    }

    public static PaymentResponse from(Payment payment, PaymentProvider.CheckoutSession checkout) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getProvider(),
                payment.getProviderRef(),
                checkout != null ? checkout.keyId() : null,
                checkout != null ? checkout.amountPaise() : null,
                checkout != null ? checkout.currency() : null
        );
    }
}
