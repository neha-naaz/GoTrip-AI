package com.tripflow.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripItineraryRequest {

    @NotNull
    @Positive
    private int dayNumber;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 500)
    private String description;

}
