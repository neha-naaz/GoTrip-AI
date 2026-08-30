package com.tripflow.trip.repository;

import com.tripflow.trip.entity.TripInclusion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripInclusionRepository extends JpaRepository<TripInclusion, Long> {

    List<TripInclusion> findByTripIdOrderByIdAsc(Long tripId);

    Optional<TripInclusion> findByIdAndTripId(Long id, Long tripId);
}
