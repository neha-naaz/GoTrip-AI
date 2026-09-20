package com.tripflow.trip.service;

import com.tripflow.agency.entity.AgencyProfile;
import com.tripflow.agency.entity.VerificationStatus;
import com.tripflow.agency.repository.AgencyProfileRepository;
import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import com.tripflow.booking.repository.BookingRepository;
import com.tripflow.trip.dto.AgencyTravelerResponse;
import com.tripflow.trip.dto.CreateTripRequest;
import com.tripflow.trip.dto.TripResponse;
import com.tripflow.trip.dto.TripWritable;
import com.tripflow.trip.dto.UpdateTripRequest;
import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripStatus;
import com.tripflow.trip.exception.AgencyNotVerifiedException;
import com.tripflow.trip.exception.AgencyProfileNotFoundException;
import com.tripflow.trip.exception.ForbiddenException;
import com.tripflow.trip.exception.TripDeletionNotAllowedException;
import com.tripflow.trip.exception.TripNotFoundException;
import com.tripflow.trip.exception.TripRulesInvalidException;
import com.tripflow.trip.repository.TripRepository;
import com.tripflow.trip.validation.TripStateValidator;
import com.tripflow.trip.validation.TripWriteValidator;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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
    private final BookingRepository bookingRepository;
    private final TripWriteValidator tripWriteValidator;
    private final TripStateValidator tripStateValidator;

    @Transactional
    public TripResponse createDraft(String userEmail, CreateTripRequest request) {
        AgencyProfile agency = requireAgencyProfile(userEmail);
        tripWriteValidator.validate(request);

        Trip trip = Trip.builder().agencyId(agency.getId()).status(TripStatus.DRAFT).build();
        applyWritableFields(trip, request);

        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse updateDraft(String userEmail, Long tripId, UpdateTripRequest request) {
        AgencyProfile agency = requireAgencyProfile(userEmail);
        Trip trip = requireOwnedTrip(tripId, agency.getId());
        tripStateValidator.requireDraft(trip, "updated");

        applyPartialFields(trip, request);
        tripWriteValidator.validateTrip(trip);
        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public void deleteTrip(String userEmail, Long tripId) {
        AgencyProfile agency = requireAgencyProfile(userEmail);
        Trip trip = requireOwnedTrip(tripId, agency.getId());

        if (trip.getStatus() == TripStatus.PUBLISHED) {
            if (bookingRepository.countByTripId(tripId) > 0) {
                throw new TripDeletionNotAllowedException("Cannot delete a published trip that has bookings");
            }
        } else if (trip.getStatus() != TripStatus.DRAFT) {
            throw new TripDeletionNotAllowedException(
                    "Only draft or unpublished published trips without bookings can be deleted");
        }

        tripRepository.delete(trip);
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
    public List<AgencyTravelerResponse> listConfirmedTravelers(String userEmail, Long tripId) {
        AgencyProfile agency = requireAgencyProfile(userEmail);
        requireOwnedTrip(tripId, agency.getId());

        List<Booking> bookings =
                bookingRepository.findByTripIdAndStatusOrderByCreatedAtAsc(tripId, BookingStatus.CONFIRMED);
        if (bookings.isEmpty()) {
            return List.of();
        }

        Map<Long, User> usersById = userRepository.findAllById(
                        bookings.stream().map(Booking::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return bookings.stream()
                .map(booking -> {
                    User traveler = usersById.get(booking.getUserId());
                    String name = traveler != null && StringUtils.hasText(traveler.getName())
                            ? traveler.getName().trim()
                            : "Traveler";
                    String email = traveler != null ? traveler.getEmail() : "";
                    return new AgencyTravelerResponse(
                            booking.getId(),
                            booking.getUserId(),
                            name,
                            email,
                            booking.getCreatedAt());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TripResponse> listAllPublishedTrips() {
        return TripResponse.from(tripRepository.findByStatusOrderByStartDateAsc(TripStatus.PUBLISHED));
    }

    @Transactional(readOnly = true)
    public List<TripResponse> searchPublished(
            String source, String destination, LocalDate startDateFrom, LocalDate startDateTo) {
        if (startDateFrom != null && startDateTo != null && startDateFrom.isAfter(startDateTo)) {
            throw new TripRulesInvalidException("startDateFrom must be on or before startDateTo");
        }

        String sourceFilter = StringUtils.hasText(source) ? source.trim() : "";
        String destinationFilter = StringUtils.hasText(destination) ? destination.trim() : "";
        // Postgres can't infer types for null bind params in lower(...); use "" / boolean flags instead.
        LocalDate from = startDateFrom != null ? startDateFrom : LocalDate.EPOCH;
        LocalDate to = startDateTo != null ? startDateTo : LocalDate.EPOCH;

        return TripResponse.from(tripRepository.searchPublished(
                TripStatus.PUBLISHED,
                sourceFilter,
                destinationFilter,
                startDateFrom != null,
                from,
                startDateTo != null,
                to));
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

    private void applyPartialFields(Trip trip, UpdateTripRequest request) {
        if (StringUtils.hasText(request.getTitle())) {
            trip.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            trip.setDescription(normalizeDescription(request.getDescription()));
        }
        if (StringUtils.hasText(request.getSource())) {
            trip.setSource(request.getSource().trim());
        }
        if (StringUtils.hasText(request.getDestination())) {
            trip.setDestination(request.getDestination().trim());
        }
        if (request.getStartDate() != null) {
            trip.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            trip.setEndDate(request.getEndDate());
        }
        if (request.getPrice() != null) {
            trip.setPrice(request.getPrice());
        }
        if (request.getBookingAmount() != null) {
            trip.setBookingAmount(request.getBookingAmount());
        }
        if (request.getCapacity() != null) {
            trip.setCapacity(request.getCapacity());
        }
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
        return tripRepository.findById(tripId).filter(trip -> Objects.equals(trip.getAgencyId(), agencyId))
                .orElseThrow(() -> new TripNotFoundException(tripId));
    }

    private void requireVerifiedAgency(AgencyProfile agency) {
        if (agency.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new AgencyNotVerifiedException();
        }
    }
}
