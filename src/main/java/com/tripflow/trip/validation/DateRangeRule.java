package com.tripflow.trip.validation;

import com.tripflow.trip.dto.TripWritable;
import com.tripflow.trip.exception.TripRulesInvalidException;
import org.springframework.stereotype.Component;

@Component
public class DateRangeRule implements TripRule {

    @Override
    public void validate(TripWritable request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new TripRulesInvalidException("End date must be on or after start date");
        }
    }
}
