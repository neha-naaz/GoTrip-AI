package com.tripflow.payment.service;

import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import com.tripflow.booking.exception.BookingNotFoundException;
import com.tripflow.booking.repository.BookingRepository;
import com.tripflow.group.service.GroupMembershipService;
import com.tripflow.payment.dto.ConfirmCheckoutRequest;
import com.tripflow.payment.dto.PaymentResponse;
import com.tripflow.payment.entity.Payment;
import com.tripflow.payment.entity.PaymentStatus;
import com.tripflow.payment.exception.PaymentNotAllowedException;
import com.tripflow.payment.exception.PaymentNotFoundException;
import com.tripflow.payment.provider.PaymentProvider;
import com.tripflow.payment.repository.PaymentRepository;
import com.tripflow.trip.exception.ForbiddenException;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final GroupMembershipService groupMembershipService;

    /**
     * Starts a payment attempt. Booking stays PENDING_PAYMENT until webhook/checkout confirms SUCCESS.
     */
    @Transactional
    public PaymentResponse initiatePayment(String userEmail, Long bookingId) {
        User user = requireCustomer(userEmail);
        Booking booking = requireOwnedPendingBooking(bookingId, user.getId());

        if (paymentRepository.existsByBookingIdAndStatus(bookingId, PaymentStatus.SUCCESS)) {
            throw new PaymentNotAllowedException("Booking already has a successful payment");
        }

        Payment existingCreated = paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId).stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.CREATED
                        && paymentProvider.getName().equals(payment.getProvider()))
                .findFirst()
                .orElse(null);

        if (existingCreated != null) {
            return toResponse(existingCreated);
        }

        PaymentProvider.CreateOrderResult order = paymentProvider.createOrder(
                new PaymentProvider.CreateOrderRequest(
                        bookingId,
                        booking.getAmountDue(),
                        "booking_" + bookingId));

        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .amount(booking.getAmountDue())
                .status(PaymentStatus.CREATED)
                .provider(paymentProvider.getName())
                .providerRef(order.providerRef())
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listForBooking(String userEmail, Long bookingId) {
        User user = requireCustomer(userEmail);
        bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        return paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    /**
     * Demo helper for MockPaymentProvider only — confirm SUCCESS without a gateway callback.
     */
    @Transactional
    public PaymentResponse sandboxConfirm(String userEmail, Long bookingId) {
        if (!paymentProvider.allowsSandboxConfirm()) {
            throw new PaymentNotAllowedException(
                    "Sandbox confirm is disabled for provider " + paymentProvider.getName());
        }

        User user = requireCustomer(userEmail);
        requireOwnedPendingBooking(bookingId, user.getId());

        Payment existingCreated = paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId).stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.CREATED)
                .findFirst()
                .orElse(null);

        String providerRef = existingCreated != null
                ? existingCreated.getProviderRef()
                : initiatePayment(userEmail, bookingId).getProviderRef();

        handleWebhook(providerRef, PaymentStatus.SUCCESS);

        Payment confirmed = paymentRepository.findByProviderRef(providerRef)
                .orElseThrow(() -> new PaymentNotFoundException(providerRef));
        return PaymentResponse.from(confirmed);
    }

    /**
     * Confirms Checkout success using Razorpay (or compatible) signature verification.
     */
    @Transactional
    public PaymentResponse confirmCheckout(String userEmail, Long bookingId, ConfirmCheckoutRequest request) {
        User user = requireCustomer(userEmail);
        requireOwnedPendingBooking(bookingId, user.getId());

        Payment payment = paymentRepository.findByProviderRef(request.getOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(request.getOrderId()));

        if (!bookingId.equals(payment.getBookingId())) {
            throw new PaymentNotAllowedException("Payment does not belong to this booking");
        }

        if (!paymentProvider.verifyCheckoutSignature(
                request.getOrderId(), request.getPaymentId(), request.getSignature())) {
            throw new PaymentNotAllowedException("Invalid payment signature");
        }

        handleWebhook(request.getOrderId(), PaymentStatus.SUCCESS);

        Payment confirmed = paymentRepository.findByProviderRef(request.getOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(request.getOrderId()));
        return toResponse(confirmed);
    }

    /**
     * Provider callback — idempotent on SUCCESS. Only CREATED payments can transition.
     */
    @Transactional
    public void handleWebhook(String providerRef, PaymentStatus status) {
        if (status != PaymentStatus.SUCCESS && status != PaymentStatus.FAILED) {
            throw new PaymentNotAllowedException("Webhook status must be SUCCESS or FAILED");
        }

        Payment payment = paymentRepository.findByProviderRef(providerRef)
                .orElseThrow(() -> new PaymentNotFoundException(providerRef));

        if (payment.getStatus() == status || payment.getStatus() != PaymentStatus.CREATED) {
            return;
        }

        if (status == PaymentStatus.FAILED) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return;
        }

        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(payment.getBookingId()));

        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            groupMembershipService.onBookingConfirmed(booking);
        }
    }

    @Transactional
    public void handleProviderWebhook(String rawBody, String signatureHeader) {
        paymentProvider.parseWebhook(rawBody, signatureHeader).ifPresent(event ->
                handleWebhook(event.providerRef(), event.status()));
    }

    private PaymentResponse toResponse(Payment payment) {
        if (payment.getStatus() != PaymentStatus.CREATED) {
            return PaymentResponse.from(payment);
        }
        return PaymentResponse.from(
                payment,
                paymentProvider.checkoutSession(payment.getProviderRef(), payment.getAmount()).orElse(null));
    }

    private User requireCustomer(String userEmail) {
        return userRepository.findByEmailAndRole(userEmail, UserRole.CUSTOMER)
                .orElseThrow(() -> new ForbiddenException("Only customers can make payments"));
    }

    private Booking requireOwnedPendingBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new PaymentNotAllowedException("Only bookings pending payment can be paid");
        }
        return booking;
    }
}
