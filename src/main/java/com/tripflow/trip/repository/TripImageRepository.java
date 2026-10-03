package com.tripflow.trip.repository;

import com.tripflow.trip.entity.TripImage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripImageRepository extends JpaRepository<TripImage, Long> {

    List<TripImage> findByTripIdOrderBySortOrderAsc(Long tripId);

    List<TripImage> findByTripIdInAndSortOrder(Collection<Long> tripIds, int sortOrder);

    long countByTripId(Long tripId);

    Optional<TripImage> findByIdAndTripId(Long id, Long tripId);

    Optional<TripImage> findFirstByTripIdOrderBySortOrderAsc(Long tripId);
}
