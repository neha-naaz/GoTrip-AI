package com.tripflow.trip.validation;

import com.tripflow.trip.dto.TripWritable;
import com.tripflow.trip.exception.TripRulesInvalidException;
import org.springframework.stereotype.Component;

@Component
public class CapacityRule implements TripRule {

    @Override
    public void validate(TripWritable request) {
        if (request.getCapacity() <= 0) {
            throw new TripRulesInvalidException("Capacity must be greater than zero");
        }
    }
}
