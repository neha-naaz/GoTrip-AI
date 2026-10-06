package com.tripflow.payment.config;

import com.tripflow.payment.provider.MockPaymentProvider;
import com.tripflow.payment.provider.PaymentProvider;
import com.tripflow.payment.provider.RazorpayPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentProviderConfig {

    private static final Logger log = LoggerFactory.getLogger(PaymentProviderConfig.class);

    @Bean
    @ConditionalOnProperty(name = "tripflow.payment.provider", havingValue = "mock")
    public PaymentProvider mockPaymentProvider() {
        log.info("Payments: MockPaymentProvider (sandbox confirm, no Razorpay modal)");
        return new MockPaymentProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "tripflow.payment.provider", havingValue = "razorpay")
    public PaymentProvider razorpayPaymentProvider(PaymentProperties paymentProperties) {
        log.info("Payments: RazorpayPaymentProvider keyId={}", paymentProperties.getRazorpay().getKeyId());
        return new RazorpayPaymentProvider(paymentProperties);
    }
}
