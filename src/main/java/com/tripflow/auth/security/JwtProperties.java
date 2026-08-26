package com.tripflow.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tripflow.jwt")
public class JwtProperties {

    private String secret;

    private long expirationMs;
}
