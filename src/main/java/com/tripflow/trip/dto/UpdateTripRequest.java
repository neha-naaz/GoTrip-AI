package com.tripflow.trip.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PUT semantics: full replacement of editable trip fields.
 * Status and agency ownership are never accepted from the client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripRequest implements TripWritable {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotBlank
    @Size(max = 100)
    private String source;

    @NotBlank
    @Size(max = 100)
    private String destination;

    @NotNull
    @FutureOrPresent
    private LocalDate startDate;

    @NotNull
    @FutureOrPresent
    private LocalDate endDate;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Positive
    private BigDecimal bookingAmount;

    @Positive
    private int capacity;
}
