package com.tripflow.payment.dto;

import com.tripflow.payment.entity.Payment;
import com.tripflow.payment.entity.PaymentStatus;
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

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getProvider(),
                payment.getProviderRef()
        );
    }
}
