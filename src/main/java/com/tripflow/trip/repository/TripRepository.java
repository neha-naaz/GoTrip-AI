package com.tripflow.trip.repository;

import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByAgencyIdOrderByCreatedAtDesc(Long agencyId);

    List<Trip> findByStatusOrderByStartDateAsc(TripStatus status);
}
