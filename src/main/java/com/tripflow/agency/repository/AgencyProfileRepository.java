package com.tripflow.agency.repository;

import com.tripflow.agency.entity.AgencyProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyProfileRepository extends JpaRepository<AgencyProfile, Long> {

    Optional<AgencyProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
