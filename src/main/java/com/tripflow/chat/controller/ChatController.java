package com.tripflow.chat.controller;

import com.tripflow.chat.dto.ChatMessageResponse;
import com.tripflow.chat.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> listHistory(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long groupId, @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(chatService.listHistory(principal.getUsername(), groupId, limit));
    }
}
