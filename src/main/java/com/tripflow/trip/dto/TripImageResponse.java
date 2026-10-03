package com.tripflow.trip.dto;

import com.tripflow.trip.entity.TripImage;
import com.tripflow.trip.entity.TripImageSource;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripImageResponse {

    private final Long id;
    private final String url;
    private final TripImageSource sourceType;
    private final int sortOrder;
    private final boolean cover;

    public static TripImageResponse from(TripImage image) {
        return new TripImageResponse(
                image.getId(),
                image.getUrl(),
                image.getSourceType(),
                image.getSortOrder(),
                image.getSortOrder() == 0);
    }

    public static List<TripImageResponse> from(List<TripImage> images) {
        return images.stream().map(TripImageResponse::from).toList();
    }
}
