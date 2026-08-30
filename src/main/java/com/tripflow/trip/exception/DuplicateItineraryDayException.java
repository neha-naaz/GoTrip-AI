package com.tripflow.trip.exception;

public class DuplicateItineraryDayException extends RuntimeException {

    public DuplicateItineraryDayException(int dayNumber) {
        super("Itinerary day " + dayNumber + " already exists for this trip");
    }
}
