package com.tripflow.common.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tripflow.cors")
public class CorsProperties {

    /**
     * Comma-separated origins are supported via Spring Boot list binding
     * (TRIPFLOW_CORS_ALLOWED_ORIGINS=https://a.com,https://b.com).
     */
    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://localhost:5173",
            "http://localhost:3000"));
}
