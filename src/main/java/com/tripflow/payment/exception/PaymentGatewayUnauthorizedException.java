package com.tripflow.payment.exception;

public class PaymentGatewayUnauthorizedException extends RuntimeException {

    public PaymentGatewayUnauthorizedException() {
        super("Razorpay authentication failed");
    }
}
