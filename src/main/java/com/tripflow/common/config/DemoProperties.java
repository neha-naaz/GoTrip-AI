package com.tripflow.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tripflow.demo")
public class DemoProperties {

    /**
     * When true, new agency registrations are VERIFIED immediately.
     * Local/compose only — keep false in real production.
     */
    private boolean autoVerifyAgencies = false;
}
