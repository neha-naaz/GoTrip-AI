package com.tripflow.payment.config;

import com.tripflow.payment.provider.MockPaymentProvider;
import com.tripflow.payment.provider.PaymentProvider;
import com.tripflow.payment.provider.RazorpayPaymentProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentProviderConfig {

    @Bean
    @ConditionalOnProperty(name = "tripflow.payment.provider", havingValue = "mock", matchIfMissing = true)
    public PaymentProvider mockPaymentProvider() {
        return new MockPaymentProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "tripflow.payment.provider", havingValue = "razorpay")
    public PaymentProvider razorpayPaymentProvider(PaymentProperties paymentProperties) {
        return new RazorpayPaymentProvider(paymentProperties);
    }
}
