package com.tripflow.media.storage;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Binary media persistence. V1 is local disk; swap for S3 later without changing TripImageService.
 */
public interface MediaStorage {

    /**
     * Stores the stream and returns a relative key like {@code {tripId}/{uuid}.jpg}.
     */
    String store(Long tripId, String originalFilename, String contentType, InputStream content, long size);

    Path resolve(String storageKey);

    void delete(String storageKey);
}
