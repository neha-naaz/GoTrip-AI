package com.tripflow.payment.exception;

public class InvalidPaymentSignatureException extends RuntimeException {

    public InvalidPaymentSignatureException() {
        super("Payment signature mismatch");
    }
}
