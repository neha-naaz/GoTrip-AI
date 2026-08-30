package com.tripflow.trip.dto;

import com.tripflow.trip.entity.TripItinerary;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripItineraryResponse {

    private final Long id;
    private final int dayNumber;
    private final String title;
    private final String description;

    public static TripItineraryResponse from(TripItinerary itinerary) {
        return new TripItineraryResponse(
                itinerary.getId(),
                itinerary.getDayNumber(),
                itinerary.getTitle(),
                itinerary.getDescription()
        );
    }

    public static List<TripItineraryResponse> from(List<TripItinerary> itineraries) {
        return itineraries.stream().map(TripItineraryResponse::from).toList();
    }
}
