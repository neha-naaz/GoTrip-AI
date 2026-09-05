package com.tripflow.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tripflow.booking.config.BookingProperties;
import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import com.tripflow.booking.repository.BookingRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingExpiryServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingExpiryService bookingExpiryService;

    @BeforeEach
    void setUp() {
        BookingProperties properties = new BookingProperties();
        properties.setPendingExpiryMinutes(15);
        bookingExpiryService = new BookingExpiryService(bookingRepository, properties);
    }

    @Test
    void expireStaleBookings_marksPendingPaymentAsExpired() {
        Booking stale = Booking.builder()
                .id(1L)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        when(bookingRepository.findByStatusAndCreatedAtBefore(
                eq(BookingStatus.PENDING_PAYMENT), any(Instant.class)))
                .thenReturn(List.of(stale));

        bookingExpiryService.expireStaleBookings();

        ArgumentCaptor<List<Booking>> captor = ArgumentCaptor.forClass(List.class);
        verify(bookingRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getStatus()).isEqualTo(BookingStatus.EXPIRED);
    }

    @Test
    void expireStaleBookings_doesNothingWhenNoStaleBookings() {
        when(bookingRepository.findByStatusAndCreatedAtBefore(
                eq(BookingStatus.PENDING_PAYMENT), any(Instant.class)))
                .thenReturn(List.of());

        bookingExpiryService.expireStaleBookings();

        verify(bookingRepository, org.mockito.Mockito.never()).saveAll(any());
    }
}
