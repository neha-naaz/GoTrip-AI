package com.tripflow.payment.service;

import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import com.tripflow.booking.exception.BookingNotFoundException;
import com.tripflow.booking.repository.BookingRepository;
import com.tripflow.payment.dto.PaymentResponse;
import com.tripflow.payment.entity.Payment;
import com.tripflow.payment.entity.PaymentStatus;
import com.tripflow.payment.exception.PaymentNotAllowedException;
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

    @Transactional
    public PaymentResponse payForBooking(String userEmail, Long bookingId) {
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
        payment = paymentRepository.save(payment);

        PaymentProvider.ChargeResult result = paymentProvider.charge(
                new PaymentProvider.ChargeRequest(bookingId, booking.getAmountDue(), providerRef));

        if (result.success()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

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
}
