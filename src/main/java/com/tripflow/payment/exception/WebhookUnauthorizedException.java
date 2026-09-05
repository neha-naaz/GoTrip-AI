package com.tripflow.payment.exception;

public class WebhookUnauthorizedException extends RuntimeException {

    public WebhookUnauthorizedException() {
        super("Invalid webhook secret");
    }
}
