package com.tripflow.booking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tripflow.booking")
public class BookingProperties {

    /** How long a PENDING_PAYMENT booking holds a seat before expiring. */
    private int pendingExpiryMinutes = 15;

    /** How often the expiry job runs (milliseconds). */
    private long expiryCheckIntervalMs = 60_000;
}
