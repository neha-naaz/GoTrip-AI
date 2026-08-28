package com.tripflow.trip.exception;

public class InvalidTripStateException extends RuntimeException {

    public InvalidTripStateException(String message) {
        super(message);
    }
}
