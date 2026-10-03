package com.tripflow.media.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tripflow.media")
public class MediaProperties {

    /** Root directory for uploaded files (relative or absolute). */
    private String rootDir = "./data/media";

    /** Max upload size in bytes (default 5MB). */
    private long maxBytes = 5_242_880L;
}
