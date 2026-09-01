package com.tripflow.booking.controller;

import com.tripflow.booking.dto.BookingResponse;
import com.tripflow.booking.dto.CreateBookingRequest;
import com.tripflow.booking.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(principal.getUsername(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> listMyBookings(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(bookingService.listMyBookings(principal.getUsername()));
    }
}
