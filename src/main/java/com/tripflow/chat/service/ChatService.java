package com.tripflow.chat.service;

import com.tripflow.chat.dto.ChatMessageResponse;
import com.tripflow.chat.entity.ChatMessage;
import com.tripflow.chat.repository.ChatMessageRepository;
import com.tripflow.group.repository.GroupMemberRepository;
import com.tripflow.trip.exception.ForbiddenException;
import com.tripflow.user.entity.User;
import com.tripflow.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int DEFAULT_HISTORY_LIMIT = 50;
    private static final int MAX_HISTORY_LIMIT = 100;
    private static final String FALLBACK_NAME = "Traveler";

    private final ChatMessageRepository chatMessageRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageResponse sendMessage(String userEmail, Long groupId, String content) {
        User user = requireUser(userEmail);
        requireMembership(groupId, user.getId());

        ChatMessage chatMessage = ChatMessage.builder()
                .senderUserId(user.getId())
                .groupId(groupId)
                .content(content.trim())
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);
        return ChatMessageResponse.from(saved, displayName(user));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> listHistory(String userEmail, Long groupId, Integer limit) {
        User user = requireUser(userEmail);
        requireMembership(groupId, user.getId());

        int pageSize = normalizeLimit(limit);
        List<ChatMessage> newestFirst =
                chatMessageRepository.findByGroupIdOrderByCreatedAtDesc(groupId, PageRequest.of(0, pageSize));

        List<ChatMessage> chronological = new ArrayList<>(newestFirst);
        Collections.reverse(chronological);
        return toResponses(chronological);
    }

    private List<ChatMessageResponse> toResponses(List<ChatMessage> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }
        Set<Long> senderIds = messages.stream().map(ChatMessage::getSenderUserId).collect(Collectors.toSet());
        Map<Long, String> namesById = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, this::displayName));

        return messages.stream()
                .map(message -> ChatMessageResponse.from(
                        message, namesById.getOrDefault(message.getSenderUserId(), FALLBACK_NAME)))
                .toList();
    }

    private String displayName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            return FALLBACK_NAME;
        }
        return user.getName().trim();
    }

    private User requireUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ForbiddenException("User not found"));
    }

    private void requireMembership(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ForbiddenException("Only group members can access chat");
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_HISTORY_LIMIT;
        }
        return Math.min(limit, MAX_HISTORY_LIMIT);
    }
}
