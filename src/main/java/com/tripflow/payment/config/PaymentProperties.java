package com.tripflow.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tripflow.payment")
public class PaymentProperties {

    private String webhookSecret = "local-dev-secret";
}
