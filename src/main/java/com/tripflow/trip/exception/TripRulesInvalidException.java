package com.tripflow.trip.exception;

public class TripRulesInvalidException extends RuntimeException {

    public TripRulesInvalidException(String message) {
        super("Invalid Trip Creation Request: " + message);
    }
}
