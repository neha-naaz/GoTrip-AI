package com.tripflow.chat.controller;

import com.tripflow.chat.dto.ChatMessageRequest;
import com.tripflow.chat.dto.ChatMessageResponse;
import com.tripflow.chat.service.ChatService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;

    @MessageMapping("/groups/{groupId}/send")
    @SendTo("/topic/groups/{groupId}")
    public ChatMessageResponse send(@DestinationVariable Long groupId, @Valid @Payload ChatMessageRequest request,
            Principal principal) {
        return chatService.sendMessage(principal.getName(), groupId, request.getContent());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception exception) {
        return exception.getMessage() != null ? exception.getMessage() : "Chat error";
    }
}
