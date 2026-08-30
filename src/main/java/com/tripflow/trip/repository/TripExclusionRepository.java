package com.tripflow.trip.repository;

import com.tripflow.trip.entity.TripExclusion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripExclusionRepository extends JpaRepository<TripExclusion, Long> {

    List<TripExclusion> findByTripIdOrderByIdAsc(Long tripId);

    Optional<TripExclusion> findByIdAndTripId(Long id, Long tripId);

}
