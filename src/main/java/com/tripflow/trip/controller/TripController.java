package com.tripflow.trip.controller;

import com.tripflow.trip.dto.CreateTripRequest;
import com.tripflow.trip.dto.TripResponse;
import com.tripflow.trip.dto.UpdateTripRequest;
import com.tripflow.trip.service.TripService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agency/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponse> createDraft(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateTripRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripService.createDraft(principal.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> listMyTrips(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(tripService.listAgencyTrips(principal.getUsername()));
    }

    @PatchMapping("/{tripId}")
    public ResponseEntity<TripResponse> patchDraft(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId, @Valid @RequestBody UpdateTripRequest request) {
        return ResponseEntity.ok(tripService.updateDraft(principal.getUsername(), tripId, request));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(@AuthenticationPrincipal UserDetails principal, @PathVariable Long tripId) {
        tripService.deleteTrip(principal.getUsername(), tripId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tripId}/publish")
    public ResponseEntity<TripResponse> publish(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(tripService.publish(principal.getUsername(), tripId));
    }
}
