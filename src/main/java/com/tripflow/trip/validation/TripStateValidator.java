package com.tripflow.trip.validation;

import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripStatus;
import com.tripflow.trip.exception.InvalidTripStateException;
import org.springframework.stereotype.Component;

@Component
public class TripStateValidator {

    public void requireDraft(Trip trip, String action) {
        if (trip.getStatus() != TripStatus.DRAFT) {
            throw new InvalidTripStateException(
                    "Only DRAFT trips can be " + action + ". Current status: " + trip.getStatus());
        }
    }
}
