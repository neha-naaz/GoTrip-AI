package com.tripflow.trip.validation;

import com.tripflow.trip.dto.TripWritable;
import com.tripflow.trip.dto.TripWritableSnapshot;
import com.tripflow.trip.entity.Trip;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TripWriteValidator {

    private final List<TripRule> rules;

    public void validate(TripWritable request) {
        rules.forEach(rule -> rule.validate(request));
    }

    public void validateTrip(Trip trip) {
        validate(TripWritableSnapshot.from(trip));
    }
}
