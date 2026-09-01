package com.tripflow.trip.exception;

public class TripDeletionNotAllowedException extends RuntimeException {

    public TripDeletionNotAllowedException(String message) {
        super(message);
    }
}
