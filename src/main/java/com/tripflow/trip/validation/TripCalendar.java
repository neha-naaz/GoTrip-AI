package com.tripflow.trip.validation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class TripCalendar {

    private TripCalendar() {
    }

    /** Inclusive trip length in days (start and end both count). */
    public static int lengthDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
