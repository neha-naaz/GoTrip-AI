package com.tripflow.trip.controller;

import com.tripflow.trip.dto.TripDetailResponse;
import com.tripflow.trip.dto.TripResponse;
import com.tripflow.trip.service.TripContentService;
import com.tripflow.trip.service.TripService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class PublicTripController {

    private final TripService tripService;
    private final TripContentService tripContentService;

    @GetMapping
    public ResponseEntity<List<TripResponse>> listPublished(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination) {
        return ResponseEntity.ok(tripService.searchPublished(source, destination));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripDetailResponse> getPublishedDetail(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripContentService.getPublishedDetail(tripId));
    }
}
