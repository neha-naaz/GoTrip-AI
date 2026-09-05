package com.tripflow.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import com.tripflow.booking.exception.BookingNotFoundException;
import com.tripflow.booking.repository.BookingRepository;
import com.tripflow.group.service.GroupMembershipService;
import com.tripflow.payment.dto.PaymentResponse;
import com.tripflow.payment.entity.Payment;
import com.tripflow.payment.entity.PaymentStatus;
import com.tripflow.payment.exception.PaymentNotAllowedException;
import com.tripflow.payment.exception.PaymentNotFoundException;
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
    @Mock
    private GroupMembershipService groupMembershipService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        PaymentProvider paymentProvider = new MockPaymentProvider();
        paymentService = new PaymentService(
                userRepository, bookingRepository, paymentRepository, paymentProvider, groupMembershipService);
    }

    private static User customer(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(UserRole.CUSTOMER);
        return user;
    }

    @Test
    void initiatePayment_createsPaymentWithoutConfirmingBooking() {
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

        PaymentResponse response = paymentService.initiatePayment("c@test.com", 1L);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(response.getProviderRef()).startsWith("mock_");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void handleWebhook_successConfirmsBooking() {
        Payment payment = Payment.builder()
                .id(1L)
                .bookingId(5L)
                .status(PaymentStatus.CREATED)
                .providerRef("mock_abc")
                .amount(new BigDecimal("3000.00"))
                .provider("MOCK")
                .build();
        Booking booking = Booking.builder()
                .id(5L)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        when(paymentRepository.findByProviderRef("mock_abc")).thenReturn(Optional.of(payment));
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        paymentService.handleWebhook("mock_abc", PaymentStatus.SUCCESS);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(paymentRepository).save(payment);
        verify(bookingRepository).save(booking);
    }

    @Test
    void handleWebhook_successIsIdempotent() {
        Payment payment = Payment.builder()
                .id(1L)
                .bookingId(5L)
                .status(PaymentStatus.SUCCESS)
                .providerRef("mock_abc")
                .build();

        when(paymentRepository.findByProviderRef("mock_abc")).thenReturn(Optional.of(payment));

        paymentService.handleWebhook("mock_abc", PaymentStatus.SUCCESS);

        verify(paymentRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void handleWebhook_failedOnlyUpdatesPayment() {
        Payment payment = Payment.builder()
                .id(1L)
                .bookingId(5L)
                .status(PaymentStatus.CREATED)
                .providerRef("mock_abc")
                .build();

        when(paymentRepository.findByProviderRef("mock_abc")).thenReturn(Optional.of(payment));

        paymentService.handleWebhook("mock_abc", PaymentStatus.FAILED);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentRepository).save(payment);
        verify(bookingRepository, never()).findById(any());
    }

    @Test
    void initiatePayment_rejectsWhenBookingAlreadyConfirmed() {
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

        assertThatThrownBy(() -> paymentService.initiatePayment("c@test.com", 1L))
                .isInstanceOf(PaymentNotAllowedException.class);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void initiatePayment_rejectsNonCustomer() {
        when(userRepository.findByEmailAndRole("a@test.com", UserRole.CUSTOMER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initiatePayment("a@test.com", 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void initiatePayment_returns404WhenBookingMissing() {
        User customer = customer(10L, "c@test.com");
        when(userRepository.findByEmailAndRole("c@test.com", UserRole.CUSTOMER))
                .thenReturn(Optional.of(customer));
        when(bookingRepository.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initiatePayment("c@test.com", 99L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void handleWebhook_unknownProviderRefReturns404() {
        when(paymentRepository.findByProviderRef("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.handleWebhook("missing", PaymentStatus.SUCCESS))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void handleWebhook_rejectsInvalidStatus() {
        assertThatThrownBy(() -> paymentService.handleWebhook("mock_abc", PaymentStatus.CREATED))
                .isInstanceOf(PaymentNotAllowedException.class);
    }
}
