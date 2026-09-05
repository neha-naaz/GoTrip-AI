package com.tripflow.payment.service;

import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import com.tripflow.booking.exception.BookingNotFoundException;
import com.tripflow.booking.repository.BookingRepository;
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
import java.util.UUID;
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

    /**
     * Starts a payment attempt. Booking stays PENDING_PAYMENT until webhook confirms SUCCESS.
     */
    @Transactional
    public PaymentResponse initiatePayment(String userEmail, Long bookingId) {
        User user = userRepository.findByEmailAndRole(userEmail, UserRole.CUSTOMER)
                .orElseThrow(() -> new ForbiddenException("Only customers can make payments"));

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new PaymentNotAllowedException("Only bookings pending payment can be paid");
        }

        if (paymentRepository.existsByBookingIdAndStatus(bookingId, PaymentStatus.SUCCESS)) {
            throw new PaymentNotAllowedException("Booking already has a successful payment");
        }

        String providerRef = "mock_" + UUID.randomUUID();

        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .amount(booking.getAmountDue())
                .status(PaymentStatus.CREATED)
                .provider(paymentProvider.getName())
                .providerRef(providerRef)
                .build();

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listForBooking(String userEmail, Long bookingId) {
        User user = userRepository.findByEmailAndRole(userEmail, UserRole.CUSTOMER)
                .orElseThrow(() -> new ForbiddenException("Only customers can view payments"));

        bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        return paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId).stream()
                .map(PaymentResponse::from)
                .toList();
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
        }
    }
}
