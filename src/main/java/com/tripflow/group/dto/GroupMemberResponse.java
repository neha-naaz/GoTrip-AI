package com.tripflow.group.dto;

import com.tripflow.group.entity.GroupMember;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupMemberResponse {

    private final Long id;
    private final Long userId;
    private final Long bookingId;
    private final Instant joinedAt;

    public static GroupMemberResponse from(GroupMember member) {
        return new GroupMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getBookingId(),
                member.getJoinedAt()
        );
    }
}
