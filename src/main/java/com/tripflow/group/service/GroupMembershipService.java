package com.tripflow.group.service;

import com.tripflow.agency.entity.AgencyProfile;
import com.tripflow.agency.repository.AgencyProfileRepository;
import com.tripflow.booking.entity.Booking;
import com.tripflow.group.dto.GroupMemberResponse;
import com.tripflow.group.dto.TripGroupResponse;
import com.tripflow.group.entity.GroupMember;
import com.tripflow.group.entity.TripGroup;
import com.tripflow.group.exception.NotGroupMemberException;
import com.tripflow.group.exception.TripGroupNotFoundException;
import com.tripflow.group.repository.GroupMemberRepository;
import com.tripflow.group.repository.TripGroupRepository;
import com.tripflow.trip.entity.Trip;
import com.tripflow.trip.repository.TripRepository;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupMembershipService {

    private static final String FALLBACK_NAME = "Traveler";

    private final TripGroupRepository tripGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final AgencyProfileRepository agencyProfileRepository;

    @Transactional
    public void onBookingConfirmed(Booking booking) {
        TripGroup tripGroup = ensureGroup(booking.getTripId());

        if (groupMemberRepository.existsByGroupIdAndUserId(tripGroup.getId(), booking.getUserId())) {
            return;
        }

        GroupMember groupMember = GroupMember.builder()
                .groupId(tripGroup.getId())
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .build();

        groupMemberRepository.save(groupMember);
    }

    @Transactional
    public TripGroupResponse getGroupForTripId(Long tripId, String userEmail) {
        User user = requireUser(userEmail);
        boolean agencyOwner = isTripAgencyOwner(user, tripId);

        TripGroup tripGroup = tripGroupRepository.findByTripId(tripId)
                .orElseGet(() -> {
                    if (!agencyOwner) {
                        throw new TripGroupNotFoundException(tripId);
                    }
                    return ensureGroup(tripId);
                });

        requireGroupAccess(user, tripGroup);
        return new TripGroupResponse(tripGroup.getId(), tripId);
    }

    @Transactional
    public List<GroupMemberResponse> listAllGroupMembers(Long tripId, String userEmail) {
        User user = requireUser(userEmail);
        boolean agencyOwner = isTripAgencyOwner(user, tripId);

        TripGroup tripGroup = tripGroupRepository.findByTripId(tripId)
                .orElseGet(() -> {
                    if (!agencyOwner) {
                        throw new TripGroupNotFoundException(tripId);
                    }
                    return ensureGroup(tripId);
                });

        requireGroupAccess(user, tripGroup);

        List<GroupMember> members = groupMemberRepository.findAllByGroupId(tripGroup.getId());
        Map<Long, String> namesById = userRepository.findAllById(
                        members.stream().map(GroupMember::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, this::displayName));

        return members.stream()
                .map(member -> GroupMemberResponse.from(
                        member, namesById.getOrDefault(member.getUserId(), FALLBACK_NAME)))
                .toList();
    }

    @Transactional(readOnly = true)
    public void requireChatAccess(String userEmail, Long groupId) {
        User user = requireUser(userEmail);
        TripGroup tripGroup = tripGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotGroupMemberException("Trip group not found"));
        requireGroupAccess(user, tripGroup);
    }

    private void requireGroupAccess(User user, TripGroup tripGroup) {
        if (groupMemberRepository.existsByGroupIdAndUserId(tripGroup.getId(), user.getId())) {
            return;
        }
        if (isTripAgencyOwner(user, tripGroup.getTripId())) {
            return;
        }
        throw new NotGroupMemberException("You are not a member of this trip group");
    }

    private boolean isTripAgencyOwner(User user, Long tripId) {
        if (user.getRole() != UserRole.AGENCY) {
            return false;
        }
        AgencyProfile agency = agencyProfileRepository.findByUserId(user.getId()).orElse(null);
        if (agency == null) {
            return false;
        }
        return tripRepository.findById(tripId)
                .map(Trip::getAgencyId)
                .filter(agencyId -> Objects.equals(agencyId, agency.getId()))
                .isPresent();
    }

    private TripGroup ensureGroup(Long tripId) {
        return tripGroupRepository.findByTripId(tripId)
                .orElseGet(() -> tripGroupRepository.save(TripGroup.builder().tripId(tripId).build()));
    }

    private User requireUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private String displayName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            return FALLBACK_NAME;
        }
        return user.getName().trim();
    }
}
