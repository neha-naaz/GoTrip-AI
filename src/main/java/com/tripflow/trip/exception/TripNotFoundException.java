package com.tripflow.trip.exception;

public class TripNotFoundException extends RuntimeException {

    public TripNotFoundException() {
        super("Trip not found");
    }

    public TripNotFoundException(Long tripId) {
        super("Trip not found: " + tripId);
    }
}
