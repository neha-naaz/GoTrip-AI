package com.tripflow.trip.validation;

import com.tripflow.trip.dto.TripWritable;
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
}
