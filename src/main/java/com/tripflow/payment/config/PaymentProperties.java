package com.tripflow.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tripflow.payment")
public class PaymentProperties {

    /**
     * Active gateway: {@code mock} (default) or {@code razorpay}.
     */
    private String provider = "mock";

    /** Shared secret for the mock/Tripflow webhook ({@code X-Tripflow-Webhook-Secret}). */
    private String webhookSecret = "local-dev-secret";

    private final Razorpay razorpay = new Razorpay();

    @Getter
    @Setter
    public static class Razorpay {
        private String keyId = "";
        private String keySecret = "";
        /** Dashboard webhook secret (HMAC of raw body). */
        private String webhookSecret = "";
        private String apiBaseUrl = "https://api.razorpay.com/v1";
    }
}
