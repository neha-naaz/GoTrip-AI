package com.tripflow.trip.entity;

/**
 * How an image entered the gallery. V1 supports UPLOAD and URL;
 * WEB / AI are reserved for later ingest implementations.
 */
public enum TripImageSource {
    UPLOAD,
    URL,
    WEB,
    AI
}
