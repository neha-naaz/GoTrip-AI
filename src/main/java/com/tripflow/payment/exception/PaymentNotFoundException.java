package com.tripflow.payment.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String providerRef) {
        super("Payment not found for provider reference: " + providerRef);
    }
}
