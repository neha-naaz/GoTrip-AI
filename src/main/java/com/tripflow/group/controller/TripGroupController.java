package com.tripflow.group.controller;

import com.tripflow.group.dto.GroupMemberResponse;
import com.tripflow.group.service.GroupMembershipService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripGroupController {

    private final GroupMembershipService groupMembershipService;

    @GetMapping("/{tripId}/group/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable Long tripId) {
        return ResponseEntity.ok(groupMembershipService.listAllGroupMembers(tripId));
    }
}
