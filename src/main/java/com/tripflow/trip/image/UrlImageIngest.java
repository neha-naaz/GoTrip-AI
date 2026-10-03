package com.tripflow.trip.image;

import com.tripflow.trip.entity.TripImageSource;
import com.tripflow.trip.exception.TripRulesInvalidException;
import java.net.URI;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UrlImageIngest implements TripImageIngest {

    @Override
    public TripImageSource source() {
        return TripImageSource.URL;
    }

    @Override
    public String ingest(Long tripId, TripImageIngestRequest request) {
        if (!StringUtils.hasText(request.url())) {
            throw new TripRulesInvalidException("Image URL is required");
        }

        String trimmed = request.url().trim();
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException ex) {
            throw new TripRulesInvalidException("Image URL is invalid");
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new TripRulesInvalidException("Image URL must start with http:// or https://");
        }
        if (!StringUtils.hasText(uri.getHost())) {
            throw new TripRulesInvalidException("Image URL is invalid");
        }
        return trimmed;
    }
}
