package com.tripflow.trip.service;

import com.tripflow.agency.entity.AgencyProfile;
import com.tripflow.agency.entity.VerificationStatus;
import com.tripflow.agency.repository.AgencyProfileRepository;
import com.tripflow.trip.dto.CreateTripRequest;
import com.tripflow.trip.dto.TripResponse;
import com.tripflow.trip.dto.TripWritable;
import com.tripflow.trip.dto.UpdateTripRequest;
import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripStatus;
import com.tripflow.trip.exception.AgencyNotVerifiedException;
import com.tripflow.trip.exception.AgencyProfileNotFoundException;
import com.tripflow.trip.exception.ForbiddenException;
import com.tripflow.trip.exception.TripNotFoundException;
import com.tripflow.trip.repository.TripRepository;
import com.tripflow.trip.validation.TripStateValidator;
import com.tripflow.trip.validation.TripWriteValidator;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final AgencyProfileRepository agencyProfileRepository;
    private final TripWriteValidator tripWriteValidator;
    private final TripStateValidator tripStateValidator;

    @Transactional
    public TripResponse createDraft(String userEmail, CreateTripRequest request) {
        AgencyProfile agency = requireAgencyProfile(userEmail);
        tripWriteValidator.validate(request);

        Trip trip = Trip.builder()
                .agencyId(agency.getId())
                .status(TripStatus.DRAFT)
                .build();
        applyWritableFields(trip, request);

        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse updateDraft(String userEmail, Long tripId, UpdateTripRequest request) {
        AgencyProfile agency = requireAgencyProfile(userEmail);
        Trip trip = requireOwnedTrip(tripId, agency.getId());
        tripStateValidator.requireDraft(trip, "updated");
        tripWriteValidator.validate(request);

        applyWritableFields(trip, request);
        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse publish(String userEmail, Long tripId) {
        AgencyProfile agency = requireAgencyProfile(userEmail);
        requireVerifiedAgency(agency);

        Trip trip = requireOwnedTrip(tripId, agency.getId());
        tripStateValidator.requireDraft(trip, "published");

        trip.setStatus(TripStatus.PUBLISHED);
        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional(readOnly = true)
    public List<TripResponse> listAgencyTrips(String userEmail) {
        AgencyProfile agency = requireAgencyProfile(userEmail);
        return TripResponse.from(tripRepository.findByAgencyIdOrderByCreatedAtDesc(agency.getId()));
    }

    @Transactional(readOnly = true)
    public List<TripResponse> listAllPublishedTrips() {
        return TripResponse.from(tripRepository.findByStatusOrderByStartDateAsc(TripStatus.PUBLISHED));
    }

    @Transactional(readOnly = true)
    public List<TripResponse> searchPublished(String source, String destination) {
        boolean hasSource = StringUtils.hasText(source);
        boolean hasDestination = StringUtils.hasText(destination);

        if (!hasSource && !hasDestination) {
            return listAllPublishedTrips();
        }

        if (hasSource && hasDestination) {
            return TripResponse.from(
                    tripRepository.findByStatusAndSourceIgnoreCaseAndDestinationIgnoreCaseOrderByStartDateAsc(
                            TripStatus.PUBLISHED, source.trim(), destination.trim()));
        }

        if (hasSource) {
            return TripResponse.from(
                    tripRepository.findByStatusAndSourceIgnoreCaseOrderByStartDateAsc(
                            TripStatus.PUBLISHED, source.trim()));
        }

        return TripResponse.from(
                tripRepository.findByStatusAndDestinationIgnoreCaseOrderByStartDateAsc(
                        TripStatus.PUBLISHED, destination.trim()));
    }

    private void applyWritableFields(Trip trip, TripWritable request) {
        trip.setTitle(request.getTitle().trim());
        trip.setDescription(normalizeDescription(request.getDescription()));
        trip.setSource(request.getSource().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setPrice(request.getPrice());
        trip.setBookingAmount(request.getBookingAmount());
        trip.setCapacity(request.getCapacity());
    }

    private String normalizeDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return null;
        }
        return description.trim();
    }

    private AgencyProfile requireAgencyProfile(String userEmail) {
        User agencyUser = userRepository.findByEmailAndRole(userEmail, UserRole.AGENCY)
                .orElseThrow(() -> new ForbiddenException("Only agency users can manage trips"));

        return agencyProfileRepository.findByUserId(agencyUser.getId())
                .orElseThrow(AgencyProfileNotFoundException::new);
    }

    /**
     * Returns the trip only if it belongs to the agency. Uses 404 for both missing and foreign trips to avoid leaking
     * existence (IDOR-safe).
     */
    private Trip requireOwnedTrip(Long tripId, Long agencyId) {
        return tripRepository.findById(tripId)
                .filter(trip -> Objects.equals(trip.getAgencyId(), agencyId))
                .orElseThrow(() -> new TripNotFoundException(tripId));
    }

    private void requireVerifiedAgency(AgencyProfile agency) {
        if (agency.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new AgencyNotVerifiedException();
        }
    }
}
