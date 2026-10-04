package com.tripflow.payment.controller;

import com.tripflow.payment.dto.ConfirmCheckoutRequest;
import com.tripflow.payment.dto.PaymentResponse;
import com.tripflow.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        return ResponseEntity.ok(paymentService.initiatePayment(principal.getUsername(), bookingId));
    }

    /**
     * Sandbox-only confirm for MockPaymentProvider. Disabled when using Razorpay.
     */
    @PostMapping("/{bookingId}/sandbox-confirm")
    public ResponseEntity<PaymentResponse> sandboxConfirm(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.sandboxConfirm(principal.getUsername(), bookingId));
    }

    /**
     * Confirms hosted checkout (Razorpay) after the browser SDK returns a signed payment.
     */
    @PostMapping("/{bookingId}/confirm-checkout")
    public ResponseEntity<PaymentResponse> confirmCheckout(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long bookingId,
            @Valid @RequestBody ConfirmCheckoutRequest request) {
        return ResponseEntity.ok(paymentService.confirmCheckout(principal.getUsername(), bookingId, request));
    }

    @GetMapping("/{bookingId}/payments")
    public ResponseEntity<List<PaymentResponse>> listPayments(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.listForBooking(principal.getUsername(), bookingId));
    }
}
