package com.tripflow.trip.dto;

import com.tripflow.trip.entity.Trip;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Read-only view of trip fields for validating business rules after a partial update.
 */
@Getter
@AllArgsConstructor
public class TripWritableSnapshot implements TripWritable {

    private final String title;
    private final String description;
    private final String source;
    private final String destination;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal price;
    private final BigDecimal bookingAmount;
    private final int capacity;

    public static TripWritableSnapshot from(Trip trip) {
        return new TripWritableSnapshot(
                trip.getTitle(),
                trip.getDescription(),
                trip.getSource(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getPrice(),
                trip.getBookingAmount(),
                trip.getCapacity()
        );
    }
}
