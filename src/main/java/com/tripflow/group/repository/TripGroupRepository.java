package com.tripflow.group.repository;

import com.tripflow.group.entity.TripGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripGroupRepository extends JpaRepository<TripGroup, Long> {

    Optional<TripGroup> findByTripId(Long tripId);
}
