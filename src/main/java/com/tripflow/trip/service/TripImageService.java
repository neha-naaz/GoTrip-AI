package com.tripflow.trip.service;

import com.tripflow.agency.entity.AgencyProfile;
import com.tripflow.agency.repository.AgencyProfileRepository;
import com.tripflow.media.storage.MediaStorage;
import com.tripflow.trip.dto.ReorderTripImagesRequest;
import com.tripflow.trip.dto.TripImageResponse;
import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.entity.TripImage;
import com.tripflow.trip.entity.TripImageSource;
import com.tripflow.trip.exception.AgencyProfileNotFoundException;
import com.tripflow.trip.exception.ForbiddenException;
import com.tripflow.trip.exception.TripImageLimitException;
import com.tripflow.trip.exception.TripNotFoundException;
import com.tripflow.trip.exception.TripRulesInvalidException;
import com.tripflow.trip.image.TripImageIngest;
import com.tripflow.trip.image.TripImageIngestRequest;
import com.tripflow.trip.repository.TripImageRepository;
import com.tripflow.trip.repository.TripRepository;
import com.tripflow.trip.validation.TripStateValidator;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TripImageService {

    public static final int MAX_IMAGES = 6;

    private final TripRepository tripRepository;
    private final TripImageRepository tripImageRepository;
    private final UserRepository userRepository;
    private final AgencyProfileRepository agencyProfileRepository;
    private final TripStateValidator tripStateValidator;
    private final MediaStorage mediaStorage;
    private final List<TripImageIngest> ingests;

    @Transactional(readOnly = true)
    public List<TripImageResponse> listForAgency(String userEmail, Long tripId) {
        requireOwnedTrip(userEmail, tripId);
        return TripImageResponse.from(tripImageRepository.findByTripIdOrderBySortOrderAsc(tripId));
    }

    @Transactional
    public TripImageResponse addUpload(String userEmail, Long tripId, MultipartFile file) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        assertUnderLimit(trip.getId());
        return persist(trip.getId(), TripImageSource.UPLOAD, TripImageIngestRequest.forUpload(file));
    }

    @Transactional
    public TripImageResponse addUrl(String userEmail, Long tripId, String url) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        assertUnderLimit(trip.getId());
        return persist(trip.getId(), TripImageSource.URL, TripImageIngestRequest.forUrl(url));
    }

    @Transactional
    public List<TripImageResponse> reorder(String userEmail, Long tripId, ReorderTripImagesRequest request) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        List<TripImage> existing = tripImageRepository.findByTripIdOrderBySortOrderAsc(trip.getId());
        List<Long> requestedIds = request.getImageIds();

        if (requestedIds.size() != existing.size()) {
            throw new TripRulesInvalidException("Reorder must include every image exactly once");
        }

        Set<Long> existingIds = existing.stream().map(TripImage::getId).collect(Collectors.toSet());
        if (!existingIds.equals(new HashSet<>(requestedIds))) {
            throw new TripRulesInvalidException("Reorder image ids do not match gallery");
        }

        Map<Long, TripImage> byId = existing.stream()
                .collect(Collectors.toMap(TripImage::getId, Function.identity()));

        // Two-phase update avoids unique (trip_id, sort_order) collisions mid-reorder.
        for (TripImage image : existing) {
            image.setSortOrder(image.getSortOrder() + 1000);
        }
        tripImageRepository.flush();

        List<TripImage> reordered = new ArrayList<>();
        for (int i = 0; i < requestedIds.size(); i++) {
            TripImage image = byId.get(requestedIds.get(i));
            image.setSortOrder(i);
            reordered.add(image);
        }
        return TripImageResponse.from(tripImageRepository.saveAll(reordered));
    }

    @Transactional
    public void delete(String userEmail, Long tripId, Long imageId) {
        Trip trip = requireOwnedDraftTrip(userEmail, tripId);
        TripImage image = tripImageRepository.findByIdAndTripId(imageId, trip.getId())
                .orElseThrow(() -> new TripNotFoundException(imageId));

        String url = image.getUrl();
        tripImageRepository.delete(image);
        tripImageRepository.flush();

        List<TripImage> remaining = tripImageRepository.findByTripIdOrderBySortOrderAsc(trip.getId());
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setSortOrder(i + 1000);
        }
        tripImageRepository.flush();
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setSortOrder(i);
        }
        tripImageRepository.saveAll(remaining);

        if (image.getSourceType() == TripImageSource.UPLOAD && url.startsWith("/api/media/")) {
            mediaStorage.delete(url.substring("/api/media/".length()));
        }
    }

    @Transactional(readOnly = true)
    public String coverUrlForTrip(Long tripId) {
        return tripImageRepository.findFirstByTripIdOrderBySortOrderAsc(tripId)
                .map(TripImage::getUrl)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> coverUrlsForTrips(List<Long> tripIds) {
        if (tripIds == null || tripIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> covers = new HashMap<>();
        for (TripImage image : tripImageRepository.findByTripIdInAndSortOrder(tripIds, 0)) {
            covers.putIfAbsent(image.getTripId(), image.getUrl());
        }
        return covers;
    }

    @Transactional(readOnly = true)
    public List<TripImageResponse> listPublicImages(Long tripId) {
        return TripImageResponse.from(tripImageRepository.findByTripIdOrderBySortOrderAsc(tripId));
    }

    private TripImageResponse persist(Long tripId, TripImageSource source, TripImageIngestRequest request) {
        TripImageIngest ingest = ingestFor(source);
        String url = ingest.ingest(tripId, request);
        int nextOrder = (int) tripImageRepository.countByTripId(tripId);

        TripImage image = TripImage.builder()
                .tripId(tripId)
                .sourceType(source)
                .url(url)
                .sortOrder(nextOrder)
                .build();
        return TripImageResponse.from(tripImageRepository.save(image));
    }

    private TripImageIngest ingestFor(TripImageSource source) {
        return ingests.stream()
                .filter(ingest -> ingest.source() == source)
                .findFirst()
                .orElseThrow(() -> new TripRulesInvalidException("Unsupported image source: " + source));
    }

    private void assertUnderLimit(Long tripId) {
        if (tripImageRepository.countByTripId(tripId) >= MAX_IMAGES) {
            throw new TripImageLimitException("A trip can have at most " + MAX_IMAGES + " images");
        }
    }

    private AgencyProfile requireAgencyProfile(String userEmail) {
        User agencyUser = userRepository.findByEmailAndRole(userEmail, UserRole.AGENCY)
                .orElseThrow(() -> new ForbiddenException("Only agency users can manage trip images"));

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
}
