package com.tripflow.payment.controller;

import com.tripflow.payment.dto.PaymentResponse;
import com.tripflow.payment.service.PaymentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{bookingId}/pay")
    public ResponseEntity<PaymentResponse> pay(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.payForBooking(principal.getUsername(), bookingId));
    }

    @GetMapping("/{bookingId}/payments")
    public ResponseEntity<List<PaymentResponse>> listPayments(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.listForBooking(principal.getUsername(), bookingId));
    }
}
