package com.tripflow.trip.controller;

import com.tripflow.trip.dto.TripItemRequest;
import com.tripflow.trip.dto.TripItemResponse;
import com.tripflow.trip.dto.TripItineraryRequest;
import com.tripflow.trip.dto.TripItineraryResponse;
import com.tripflow.trip.service.TripContentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agency/trips/{tripId}")
@RequiredArgsConstructor
public class TripContentController {

    private final TripContentService tripContentService;

    // ---- Itineraries ----

    @PostMapping("/itineraries")
    public ResponseEntity<TripItineraryResponse> createItinerary(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @Valid @RequestBody TripItineraryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripContentService.createItinerary(principal.getUsername(), tripId, request));
    }

    @GetMapping("/itineraries")
    public ResponseEntity<List<TripItineraryResponse>> listItineraries(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(tripContentService.listItineraries(principal.getUsername(), tripId));
    }

    @PutMapping("/itineraries/{itineraryId}")
    public ResponseEntity<TripItineraryResponse> updateItinerary(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @PathVariable Long itineraryId,
            @Valid @RequestBody TripItineraryRequest request) {
        return ResponseEntity.ok(
                tripContentService.updateItinerary(principal.getUsername(), tripId, itineraryId, request));
    }

    @DeleteMapping("/itineraries/{itineraryId}")
    public ResponseEntity<Void> deleteItinerary(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @PathVariable Long itineraryId) {
        tripContentService.deleteItinerary(principal.getUsername(), tripId, itineraryId);
        return ResponseEntity.noContent().build();
    }

    // ---- Inclusions ----

    @PostMapping("/inclusions")
    public ResponseEntity<TripItemResponse> createInclusion(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @Valid @RequestBody TripItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripContentService.createInclusion(principal.getUsername(), tripId, request));
    }

    @GetMapping("/inclusions")
    public ResponseEntity<List<TripItemResponse>> listInclusions(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(tripContentService.listInclusions(principal.getUsername(), tripId));
    }

    @PutMapping("/inclusions/{inclusionId}")
    public ResponseEntity<TripItemResponse> updateInclusion(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @PathVariable Long inclusionId, @Valid @RequestBody TripItemRequest request) {
        return ResponseEntity.ok(
                tripContentService.updateInclusion(principal.getUsername(), tripId, inclusionId, request));
    }

    @DeleteMapping("/inclusions/{inclusionId}")
    public ResponseEntity<Void> deleteInclusion(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @PathVariable Long inclusionId) {
        tripContentService.deleteInclusion(principal.getUsername(), tripId, inclusionId);
        return ResponseEntity.noContent().build();
    }

    // ---- Exclusions ----

    @PostMapping("/exclusions")
    public ResponseEntity<TripItemResponse> createExclusion(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @Valid @RequestBody TripItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripContentService.createExclusion(principal.getUsername(), tripId, request));
    }

    @GetMapping("/exclusions")
    public ResponseEntity<List<TripItemResponse>> listExclusions(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(tripContentService.listExclusions(principal.getUsername(), tripId));
    }

    @PutMapping("/exclusions/{exclusionId}")
    public ResponseEntity<TripItemResponse> updateExclusion(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @PathVariable Long exclusionId, @Valid @RequestBody TripItemRequest request) {
        return ResponseEntity.ok(
                tripContentService.updateExclusion(principal.getUsername(), tripId, exclusionId, request));
    }

    @DeleteMapping("/exclusions/{exclusionId}")
    public ResponseEntity<Void> deleteExclusion(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @PathVariable Long exclusionId) {
        tripContentService.deleteExclusion(principal.getUsername(), tripId, exclusionId);
        return ResponseEntity.noContent().build();
    }
}
