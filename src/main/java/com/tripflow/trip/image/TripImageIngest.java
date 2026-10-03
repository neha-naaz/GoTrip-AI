package com.tripflow.trip.image;

import com.tripflow.trip.entity.TripImageSource;

/**
 * Pluggable image intake. V1: UPLOAD + URL. Later: WEB / AI without changing TripImageService.
 */
public interface TripImageIngest {

    TripImageSource source();

    /**
     * Produces a public URL (or /api/media/... path) to store on {@code trip_images.url}.
     */
    String ingest(Long tripId, TripImageIngestRequest request);
}
