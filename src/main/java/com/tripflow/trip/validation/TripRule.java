package com.tripflow.trip.validation;

import com.tripflow.trip.dto.TripWritable;

public interface TripRule {

    void validate(TripWritable request);
}
