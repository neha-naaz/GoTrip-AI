package com.tripflow.payment.repository;

import com.tripflow.payment.entity.Payment;
import com.tripflow.payment.entity.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    Optional<Payment> findByProviderRef(String providerRef);

    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
