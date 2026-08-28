package com.tripflow.trip.exception;

public class AgencyNotVerifiedException extends RuntimeException {

    public AgencyNotVerifiedException() {
        super("Agency must be verified to publish trips");
    }
}
