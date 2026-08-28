package com.tripflow.trip.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("You are not authorized to perform this operation");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
