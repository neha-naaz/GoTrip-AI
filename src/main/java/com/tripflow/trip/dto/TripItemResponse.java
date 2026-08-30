package com.tripflow.trip.dto;

import com.tripflow.trip.entity.TripExclusion;
import com.tripflow.trip.entity.TripInclusion;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripItemResponse {

    private final Long id;
    private final String description;

    public static TripItemResponse from(TripInclusion inclusion) {
        return new TripItemResponse(inclusion.getId(), inclusion.getDescription());
    }

    public static TripItemResponse from(TripExclusion exclusion) {
        return new TripItemResponse(exclusion.getId(), exclusion.getDescription());
    }

    public static List<TripItemResponse> fromInclusions(List<TripInclusion> inclusions) {
        return inclusions.stream().map(TripItemResponse::from).toList();
    }

    public static List<TripItemResponse> fromExclusions(List<TripExclusion> exclusions) {
        return exclusions.stream().map(TripItemResponse::from).toList();
    }
}
