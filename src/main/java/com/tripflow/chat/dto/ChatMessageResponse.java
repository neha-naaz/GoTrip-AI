package com.tripflow.chat.dto;

import com.tripflow.chat.entity.ChatMessage;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private final Long id;
    private final Long groupId;
    private final Long senderUserId;
    private final String senderName;
    private final String content;
    private final Instant createdAt;

    public static ChatMessageResponse from(ChatMessage chatMessage, String senderName) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getGroupId(),
                chatMessage.getSenderUserId(),
                senderName,
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }
}
