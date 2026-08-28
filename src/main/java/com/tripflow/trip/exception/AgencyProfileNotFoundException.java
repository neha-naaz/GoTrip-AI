package com.tripflow.trip.exception;

public class AgencyProfileNotFoundException extends RuntimeException {

    public AgencyProfileNotFoundException() {
        super("Agency profile not found");
    }
}
