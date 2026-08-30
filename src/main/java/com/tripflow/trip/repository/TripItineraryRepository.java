package com.tripflow.trip.repository;

import com.tripflow.trip.entity.TripItinerary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripItineraryRepository extends JpaRepository<TripItinerary, Long> {

    List<TripItinerary> findByTripIdOrderByDayNumberAsc(Long tripId);

    boolean existsByTripIdAndDayNumber(Long tripId, int dayNumber);

    boolean existsByTripIdAndDayNumberAndIdNot(Long tripId, int dayNumber, Long id);

    Optional<TripItinerary> findByIdAndTripId(Long id, Long tripId);

}
