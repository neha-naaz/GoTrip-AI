package com.tripflow.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shared write contract for create/update trip payloads.
 * Keeps business validation rules reusable across DTOs.
 */
public interface TripWritable {

    String getTitle();

    String getDescription();

    String getSource();

    String getDestination();

    LocalDate getStartDate();

    LocalDate getEndDate();

    BigDecimal getPrice();

    BigDecimal getBookingAmount();

    int getCapacity();
}
