package com.tripflow.trip.repository;

import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByAgencyIdOrderByCreatedAtDesc(Long agencyId);

    List<Trip> findByStatusOrderByStartDateAsc(TripStatus status);

    List<Trip> findByStatusAndSourceIgnoreCaseAndDestinationIgnoreCaseOrderByStartDateAsc(
            TripStatus status, String source, String destination);

    List<Trip> findByStatusAndSourceIgnoreCaseOrderByStartDateAsc(TripStatus status, String source);

    List<Trip> findByStatusAndDestinationIgnoreCaseOrderByStartDateAsc(TripStatus status, String destination);

    Optional<Trip> findByIdAndStatus(Long id, TripStatus status);
}
