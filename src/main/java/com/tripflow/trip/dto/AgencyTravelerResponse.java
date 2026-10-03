package com.tripflow.trip.dto;

import java.time.Instant;

public record AgencyTravelerResponse(Long bookingId, Long userId, String name, String email, Instant bookedAt) {

}
