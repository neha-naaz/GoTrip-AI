package com.tripflow.trip.controller;

import com.tripflow.trip.dto.TripResponse;
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

    /**
     * Public catalog: PUBLISHED trips only.
     * Optional filters: source, destination (case-insensitive).
     */
    @GetMapping
    public ResponseEntity<List<TripResponse>> listPublished(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination) {
        return ResponseEntity.ok(tripService.searchPublished(source, destination));
    }

    /**
     * Public detail: returns 404 for missing or non-published trips.
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getPublished(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripService.getPublishedById(tripId));
    }
}
