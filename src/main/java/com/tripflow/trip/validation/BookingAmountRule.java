package com.tripflow.trip.validation;

import com.tripflow.trip.dto.TripWritable;
import com.tripflow.trip.exception.TripRulesInvalidException;
import org.springframework.stereotype.Component;

@Component
public class BookingAmountRule implements TripRule {

    @Override
    public void validate(TripWritable request) {
        if (request.getBookingAmount().compareTo(request.getPrice()) > 0) {
            throw new TripRulesInvalidException("Booking amount cannot exceed trip price");
        }
    }
}
