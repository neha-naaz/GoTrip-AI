package com.tripflow.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import com.tripflow.booking.exception.BookingNotFoundException;
import com.tripflow.booking.repository.BookingRepository;
import com.tripflow.payment.dto.PaymentResponse;
import com.tripflow.payment.entity.Payment;
import com.tripflow.payment.entity.PaymentStatus;
import com.tripflow.payment.exception.PaymentNotAllowedException;
import com.tripflow.payment.provider.MockPaymentProvider;
import com.tripflow.payment.provider.PaymentProvider;
import com.tripflow.payment.repository.PaymentRepository;
import com.tripflow.trip.exception.ForbiddenException;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        PaymentProvider paymentProvider = new MockPaymentProvider();
        paymentService = new PaymentService(
                userRepository, bookingRepository, paymentRepository, paymentProvider);
    }

    private static User customer(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(UserRole.CUSTOMER);
        return user;
    }

    @Test
    void payForBooking_confirmsBookingWhenMockProviderSucceeds() {
        User customer = customer(10L, "c@test.com");
        Booking booking = Booking.builder()
                .id(1L)
                .userId(10L)
                .tripId(5L)
                .status(BookingStatus.PENDING_PAYMENT)
                .amountDue(new BigDecimal("3000.00"))
                .build();

        when(userRepository.findByEmailAndRole("c@test.com", UserRole.CUSTOMER))
                .thenReturn(Optional.of(customer));
        when(bookingRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingIdAndStatus(1L, PaymentStatus.SUCCESS)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(100L);
            }
            return p;
        });

        PaymentResponse response = paymentService.payForBooking("c@test.com", 1L);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getAmount()).isEqualByComparingTo("3000.00");
        assertThat(response.getProvider()).isEqualTo("MOCK");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void payForBooking_rejectsWhenBookingAlreadyConfirmed() {
        User customer = customer(10L, "c@test.com");
        Booking booking = Booking.builder()
                .id(1L)
                .userId(10L)
                .status(BookingStatus.CONFIRMED)
                .amountDue(new BigDecimal("3000.00"))
                .build();

        when(userRepository.findByEmailAndRole("c@test.com", UserRole.CUSTOMER))
                .thenReturn(Optional.of(customer));
        when(bookingRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.payForBooking("c@test.com", 1L))
                .isInstanceOf(PaymentNotAllowedException.class);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void payForBooking_rejectsNonCustomer() {
        when(userRepository.findByEmailAndRole("a@test.com", UserRole.CUSTOMER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.payForBooking("a@test.com", 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void payForBooking_returns404WhenBookingMissing() {
        User customer = customer(10L, "c@test.com");
        when(userRepository.findByEmailAndRole("c@test.com", UserRole.CUSTOMER))
                .thenReturn(Optional.of(customer));
        when(bookingRepository.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.payForBooking("c@test.com", 99L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void payForBooking_persistsSuccessPaymentWithMockProviderRef() {
        User customer = customer(10L, "c@test.com");
        Booking booking = Booking.builder()
                .id(1L)
                .userId(10L)
                .status(BookingStatus.PENDING_PAYMENT)
                .amountDue(new BigDecimal("500.00"))
                .build();

        when(userRepository.findByEmailAndRole("c@test.com", UserRole.CUSTOMER))
                .thenReturn(Optional.of(customer));
        when(bookingRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingIdAndStatus(1L, PaymentStatus.SUCCESS)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(1L);
            }
            return p;
        });

        paymentService.payForBooking("c@test.com", 1L);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, atLeastOnce()).save(captor.capture());
        Payment last = captor.getValue();
        assertThat(last.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(last.getProviderRef()).startsWith("mock_");
    }
}
