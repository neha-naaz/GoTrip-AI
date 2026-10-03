package com.tripflow.trip.image;

import org.springframework.web.multipart.MultipartFile;

/**
 * Input payload for an ingest strategy. Only the fields relevant to that source are set.
 */
public record TripImageIngestRequest(MultipartFile file, String url) {

    public static TripImageIngestRequest forUpload(MultipartFile file) {
        return new TripImageIngestRequest(file, null);
    }

    public static TripImageIngestRequest forUrl(String url) {
        return new TripImageIngestRequest(null, url);
    }
}
