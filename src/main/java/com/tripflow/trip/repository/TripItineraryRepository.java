package com.tripflow.trip.repository;

import com.tripflow.trip.entity.TripItinerary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripItineraryRepository extends JpaRepository<TripItinerary, Long> {

}
