package com.tripflow.trip.service;

import com.tripflow.agency.entity.AgencyProfile;
import com.tripflow.agency.repository.AgencyProfileRepository;
import com.tripflow.trip.dto.TripDetailResponse;
import com.tripflow.trip.dto.TripItemRequest;
import com.tripflow.trip.dto.TripItemResponse;
import com.tripflow.trip.dto.TripItineraryRequest;
import com.tripflow.trip.dto.TripItineraryResponse;
import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripExclusion;
import com.tripflow.trip.entity.TripInclusion;
import com.tripflow.trip.entity.TripItinerary;
import com.tripflow.trip.entity.TripStatus;
import com.tripflow.trip.exception.AgencyProfileNotFoundException;
import com.tripflow.trip.exception.DuplicateItineraryDayException;
import com.tripflow.trip.exception.ForbiddenException;
import com.tripflow.trip.exception.TripNotFoundException;
import com.tripflow.trip.repository.TripExclusionRepository;
import com.tripflow.trip.repository.TripInclusionRepository;
import com.tripflow.trip.repository.TripItineraryRepository;
import com.tripflow.trip.repository.TripRepository;
import com.tripflow.trip.validation.TripStateValidator;
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
public class TripContentService {

    private final TripRepository tripRepository;
    private final TripItineraryRepository tripItineraryRepository;
    private final TripInclusionRepository tripInclusionRepository;
    private final TripExclusionRepository tripExclusionRepository;
    private final UserRepository userRepository;
    private final AgencyProfileRepository agencyProfileRepository;
    private final TripStateValidator tripStateValidator;

    // ---- Itinerary ----

    @Transactional
    public TripItineraryResponse createItinerary(String userEmail, Long tripId, TripItineraryRequest request) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        assertUniqueDay(trip.getId(), request.getDayNumber(), null);

        TripItinerary itinerary = TripItinerary.builder()
                .tripId(trip.getId())
                .dayNumber(request.getDayNumber())
                .title(request.getTitle().trim())
                .description(normalizeOptionalText(request.getDescription()))
                .build();

        return TripItineraryResponse.from(tripItineraryRepository.save(itinerary));
    }

    @Transactional(readOnly = true)
    public List<TripItineraryResponse> listItineraries(String userEmail, Long tripId) {
        requireOwnedTrip(userEmail, tripId);
        return TripItineraryResponse.from(tripItineraryRepository.findByTripIdOrderByDayNumberAsc(tripId));
    }

    @Transactional
    public TripItineraryResponse updateItinerary(
            String userEmail, Long tripId, Long itineraryId, TripItineraryRequest request) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        TripItinerary itinerary = requireItinerary(itineraryId, trip.getId());
        assertUniqueDay(trip.getId(), request.getDayNumber(), itineraryId);

        itinerary.setDayNumber(request.getDayNumber());
        itinerary.setTitle(request.getTitle().trim());
        itinerary.setDescription(normalizeOptionalText(request.getDescription()));
        return TripItineraryResponse.from(tripItineraryRepository.save(itinerary));
    }

    @Transactional
    public void deleteItinerary(String userEmail, Long tripId, Long itineraryId) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        TripItinerary itinerary = requireItinerary(itineraryId, trip.getId());
        tripItineraryRepository.delete(itinerary);
    }

    // ---- Inclusions ----

    @Transactional
    public TripItemResponse createInclusion(String userEmail, Long tripId, TripItemRequest request) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        TripInclusion inclusion = TripInclusion.builder()
                .tripId(trip.getId())
                .description(request.getDescription().trim())
                .build();
        return TripItemResponse.from(tripInclusionRepository.save(inclusion));
    }

    @Transactional(readOnly = true)
    public List<TripItemResponse> listInclusions(String userEmail, Long tripId) {
        requireOwnedTrip(userEmail, tripId);
        return TripItemResponse.fromInclusions(tripInclusionRepository.findByTripIdOrderByIdAsc(tripId));
    }

    @Transactional
    public TripItemResponse updateInclusion(
            String userEmail, Long tripId, Long inclusionId, TripItemRequest request) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        TripInclusion inclusion = requireInclusion(inclusionId, trip.getId());
        inclusion.setDescription(request.getDescription().trim());
        return TripItemResponse.from(tripInclusionRepository.save(inclusion));
    }

    @Transactional
    public void deleteInclusion(String userEmail, Long tripId, Long inclusionId) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        TripInclusion inclusion = requireInclusion(inclusionId, trip.getId());
        tripInclusionRepository.delete(inclusion);
    }

    // ---- Exclusions ----

    @Transactional
    public TripItemResponse createExclusion(String userEmail, Long tripId, TripItemRequest request) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        TripExclusion exclusion = TripExclusion.builder()
                .tripId(trip.getId())
                .description(request.getDescription().trim())
                .build();
        return TripItemResponse.from(tripExclusionRepository.save(exclusion));
    }

    @Transactional(readOnly = true)
    public List<TripItemResponse> listExclusions(String userEmail, Long tripId) {
        requireOwnedTrip(userEmail, tripId);
        return TripItemResponse.fromExclusions(tripExclusionRepository.findByTripIdOrderByIdAsc(tripId));
    }

    @Transactional
    public TripItemResponse updateExclusion(
            String userEmail, Long tripId, Long exclusionId, TripItemRequest request) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        TripExclusion exclusion = requireExclusion(exclusionId, trip.getId());
        exclusion.setDescription(request.getDescription().trim());
        return TripItemResponse.from(tripExclusionRepository.save(exclusion));
    }

    @Transactional
    public void deleteExclusion(String userEmail, Long tripId, Long exclusionId) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        TripExclusion exclusion = requireExclusion(exclusionId, trip.getId());
        tripExclusionRepository.delete(exclusion);
    }

    // ---- Public detail ----

    @Transactional(readOnly = true)
    public TripDetailResponse getPublishedDetail(Long tripId) {
        Trip trip = tripRepository.findByIdAndStatus(tripId, TripStatus.PUBLISHED)
                .orElseThrow(() -> new TripNotFoundException(tripId));

        return TripDetailResponse.from(
                trip,
                tripItineraryRepository.findByTripIdOrderByDayNumberAsc(tripId),
                tripInclusionRepository.findByTripIdOrderByIdAsc(tripId),
                tripExclusionRepository.findByTripIdOrderByIdAsc(tripId)
        );
    }

    // ---- Helpers ----

    private AgencyProfile requireAgencyProfile(String userEmail) {
        User agencyUser = userRepository.findByEmailAndRole(userEmail, UserRole.AGENCY)
                .orElseThrow(() -> new ForbiddenException("Only agency users can manage trip content"));

        return agencyProfileRepository.findByUserId(agencyUser.getId())
                .orElseThrow(AgencyProfileNotFoundException::new);
    }

    private Trip requireOwnedTrip(String userEmail, Long tripId) {
        AgencyProfile agency = requireAgencyProfile(userEmail);
        return tripRepository.findById(tripId)
                .filter(trip -> Objects.equals(trip.getAgencyId(), agency.getId()))
                .orElseThrow(() -> new TripNotFoundException(tripId));
    }

    private Trip requireOwnedDraftTrip(String userEmail, Long tripId) {
        Trip trip = requireOwnedTrip(userEmail, tripId);
        tripStateValidator.requireDraft(trip, "modified");
        return trip;
    }

    private void assertUniqueDay(Long tripId, int dayNumber, Long excludeItineraryId) {
        boolean duplicate = excludeItineraryId == null
                ? tripItineraryRepository.existsByTripIdAndDayNumber(tripId, dayNumber)
                : tripItineraryRepository.existsByTripIdAndDayNumberAndIdNot(tripId, dayNumber, excludeItineraryId);

        if (duplicate) {
            throw new DuplicateItineraryDayException(dayNumber);
        }
    }

    private TripItinerary requireItinerary(Long itineraryId, Long tripId) {
        return tripItineraryRepository.findByIdAndTripId(itineraryId, tripId)
                .orElseThrow(TripNotFoundException::new);
    }

    private TripInclusion requireInclusion(Long inclusionId, Long tripId) {
        return tripInclusionRepository.findByIdAndTripId(inclusionId, tripId)
                .orElseThrow(TripNotFoundException::new);
    }

    private TripExclusion requireExclusion(Long exclusionId, Long tripId) {
        return tripExclusionRepository.findByIdAndTripId(exclusionId, tripId)
                .orElseThrow(TripNotFoundException::new);
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
