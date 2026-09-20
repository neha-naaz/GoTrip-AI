package com.tripflow.trip.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgencyTravelerResponse {

    private final Long bookingId;
    private final Long userId;
    private final String name;
    private final String email;
    private final Instant bookedAt;
}
