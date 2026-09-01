package com.tripflow.trip.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PATCH semantics: only non-null fields are applied.
 * Status and agency ownership are never accepted from the client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    @Size(max = 100)
    private String source;

    @Size(max = 100)
    private String destination;

    @FutureOrPresent
    private LocalDate startDate;

    @FutureOrPresent
    private LocalDate endDate;

    @Positive
    private BigDecimal price;

    @Positive
    private BigDecimal bookingAmount;

    @Positive
    private Integer capacity;
}
