package com.tripflow.chat.dto;

import com.tripflow.chat.entity.ChatMessage;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private final Long id;
    private final Long groupId;
    private final Long senderUserId;
    private final String content;
    private final Instant createdAt;

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getGroupId(),
                chatMessage.getSenderUserId(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }

    public static List<ChatMessageResponse> from(List<ChatMessage> chatMessages) {
        return chatMessages.stream().map(ChatMessageResponse::from).toList();
    }
}
