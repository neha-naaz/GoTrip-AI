package com.tripflow.trip.repository;

import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByAgencyIdOrderByCreatedAtDesc(Long agencyId);

    List<Trip> findByStatusOrderByStartDateAsc(TripStatus status);

    List<Trip> findByStatusAndSourceIgnoreCaseAndDestinationIgnoreCaseOrderByStartDateAsc(
            TripStatus status, String source, String destination);

    List<Trip> findByStatusAndSourceIgnoreCaseOrderByStartDateAsc(TripStatus status, String source);

    List<Trip> findByStatusAndDestinationIgnoreCaseOrderByStartDateAsc(TripStatus status, String destination);

    Optional<Trip> findByIdAndStatus(Long id, TripStatus status);

    @Query("""
            select t from Trip t
            where t.status = :status
              and (:source = '' or lower(t.source) = lower(:source))
              and (:destination = '' or lower(t.destination) = lower(:destination))
              and (:hasStartDateFrom = false or t.startDate >= :startDateFrom)
              and (:hasStartDateTo = false or t.startDate <= :startDateTo)
            order by t.startDate asc
            """)
    List<Trip> searchPublished(
            @Param("status") TripStatus status,
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("hasStartDateFrom") boolean hasStartDateFrom,
            @Param("startDateFrom") LocalDate startDateFrom,
            @Param("hasStartDateTo") boolean hasStartDateTo,
            @Param("startDateTo") LocalDate startDateTo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t where t.id = :id and t.status = :status")
    Optional<Trip> findByIdAndStatusForUpdate(@Param("id") Long id, @Param("status") TripStatus status);
}
