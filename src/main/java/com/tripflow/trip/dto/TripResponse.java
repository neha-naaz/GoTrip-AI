package com.tripflow.trip.dto;

import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripResponse {

    private final Long id;
    private final Long agencyId;
    private final String title;
    private final String description;
    private final String source;
    private final String destination;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal price;
    private final BigDecimal bookingAmount;
    private final int capacity;
    private final TripStatus status;
    private final Instant createdAt;

    public static TripResponse from(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getAgencyId(),
                trip.getTitle(),
                trip.getDescription(),
                trip.getSource(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getPrice(),
                trip.getBookingAmount(),
                trip.getCapacity(),
                trip.getStatus(),
                trip.getCreatedAt()
        );
    }

    public static List<TripResponse> from(List<Trip> trips) {
        return trips.stream()
                .map(TripResponse::from)
                .toList();
    }
}
