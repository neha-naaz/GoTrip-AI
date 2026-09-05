package com.tripflow.group.service;

import com.tripflow.booking.entity.Booking;
import com.tripflow.group.dto.GroupMemberResponse;
import com.tripflow.group.entity.GroupMember;
import com.tripflow.group.entity.TripGroup;
import com.tripflow.group.exception.TripGroupNotFoundException;
import com.tripflow.group.repository.GroupMemberRepository;
import com.tripflow.group.repository.TripGroupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupMembershipService {

    private final TripGroupRepository tripGroupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public void onBookingConfirmed(Booking booking) {
        TripGroup tripGroup = tripGroupRepository.findByTripId(booking.getTripId())
                .orElseGet(() -> tripGroupRepository.save(
                        TripGroup.builder().tripId(booking.getTripId()).build()));

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

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> listAllGroupMembers(Long tripId) {
        TripGroup tripGroup = tripGroupRepository.findByTripId(tripId)
                .orElseThrow(() -> new TripGroupNotFoundException(tripId));

        return groupMemberRepository.findAllByGroupId(tripGroup.getId()).stream()
                .map(GroupMemberResponse::from)
                .toList();
    }
}
