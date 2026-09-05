package com.tripflow.booking.service;

import com.tripflow.booking.config.BookingProperties;
import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import com.tripflow.booking.repository.BookingRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryService {

    private final BookingRepository bookingRepository;
    private final BookingProperties bookingProperties;

    @Scheduled(fixedDelayString = "${tripflow.booking.expiry-check-interval-ms:60000}")
    @Transactional
    public void expireStaleBookings() {
        Instant cutoff = Instant.now().minus(bookingProperties.getPendingExpiryMinutes(), ChronoUnit.MINUTES);
        List<Booking> stale =
                bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, cutoff);

        if (stale.isEmpty()) {
            return;
        }

        for (Booking booking : stale) {
            booking.setStatus(BookingStatus.EXPIRED);
        }
        bookingRepository.saveAll(stale);
        log.info("Expired {} stale PENDING_PAYMENT booking(s)", stale.size());
    }
}
