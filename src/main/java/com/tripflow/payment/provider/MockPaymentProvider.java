package com.tripflow.payment.provider;

import org.springframework.stereotype.Component;

/**
 * Sandbox provider — always succeeds. Replace with a real gateway later.
 */
@Component
public class MockPaymentProvider implements PaymentProvider {

    public static final String NAME = "MOCK";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ChargeResult charge(ChargeRequest request) {
        return new ChargeResult(true, request.providerRef());
    }
}
