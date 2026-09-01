package com.tripflow.booking.repository;

import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByTripIdAndStatusIn(Long tripId, Collection<BookingStatus> statuses);

    long countByTripId(Long tripId);

    boolean existsByTripIdAndUserIdAndStatusIn(Long tripId, Long userId, Collection<BookingStatus> statuses);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);
}
