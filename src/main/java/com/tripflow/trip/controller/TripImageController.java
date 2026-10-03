package com.tripflow.trip.controller;

import com.tripflow.trip.dto.AddTripImageUrlRequest;
import com.tripflow.trip.dto.ReorderTripImagesRequest;
import com.tripflow.trip.dto.TripImageResponse;
import com.tripflow.trip.service.TripImageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/agency/trips/{tripId}/images")
@RequiredArgsConstructor
public class TripImageController {

    private final TripImageService tripImageService;

    @GetMapping
    public ResponseEntity<List<TripImageResponse>> list(
            @AuthenticationPrincipal UserDetails principal, @PathVariable Long tripId) {
        return ResponseEntity.ok(tripImageService.listForAgency(principal.getUsername(), tripId));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TripImageResponse> upload(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripImageService.addUpload(principal.getUsername(), tripId, file));
    }

    @PostMapping("/url")
    public ResponseEntity<TripImageResponse> addUrl(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId,
            @Valid @RequestBody AddTripImageUrlRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripImageService.addUrl(principal.getUsername(), tripId, request.getUrl()));
    }

    @PutMapping("/reorder")
    public ResponseEntity<List<TripImageResponse>> reorder(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId,
            @Valid @RequestBody ReorderTripImagesRequest request) {
        return ResponseEntity.ok(tripImageService.reorder(principal.getUsername(), tripId, request));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long tripId,
            @PathVariable Long imageId) {
        tripImageService.delete(principal.getUsername(), tripId, imageId);
        return ResponseEntity.noContent().build();
    }
}
