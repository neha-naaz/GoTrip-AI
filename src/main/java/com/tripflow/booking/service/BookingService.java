package com.tripflow.booking.service;

import com.tripflow.booking.dto.BookingResponse;
import com.tripflow.booking.dto.CreateBookingRequest;
import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import com.tripflow.booking.exception.BookingNotAllowedException;
import com.tripflow.booking.exception.TripFullException;
import com.tripflow.booking.repository.BookingRepository;
import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripStatus;
import com.tripflow.trip.exception.ForbiddenException;
import com.tripflow.trip.exception.TripNotFoundException;
import com.tripflow.trip.repository.TripRepository;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final List<BookingStatus> RESERVED_STATUSES =
            List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED);

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public BookingResponse create(String userEmail, CreateBookingRequest request) {
        User user = userRepository.findByEmailAndRole(userEmail, UserRole.CUSTOMER)
                .orElseThrow(() -> new ForbiddenException("Only customers can book trips"));

        Long tripId = request.getTripId();
        Trip trip = tripRepository.findByIdAndStatusForUpdate(tripId, TripStatus.PUBLISHED)
                .orElseThrow(() -> new TripNotFoundException(tripId));

        if (bookingRepository.existsByTripIdAndUserIdAndStatusIn(tripId, user.getId(), RESERVED_STATUSES)) {
            throw new BookingNotAllowedException("You already have an active booking for this trip");
        }

        long reservedSeats = bookingRepository.countByTripIdAndStatusIn(tripId, RESERVED_STATUSES);
        if (reservedSeats >= trip.getCapacity()) {
            throw new TripFullException("Trip is full. No new bookings can be made");
        }

        Booking booking = Booking.builder()
                .tripId(tripId)
                .userId(user.getId())
                .status(BookingStatus.PENDING_PAYMENT)
                .amountDue(trip.getBookingAmount())
                .build();

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listMyBookings(String userEmail) {
        User user = userRepository.findByEmailAndRole(userEmail, UserRole.CUSTOMER)
                .orElseThrow(() -> new ForbiddenException("Only customers can view bookings"));

        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(BookingResponse::from)
                .toList();
    }
}
