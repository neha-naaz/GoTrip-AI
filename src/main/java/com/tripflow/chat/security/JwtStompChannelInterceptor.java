package com.tripflow.chat.security;

import com.tripflow.auth.security.JwtService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Authenticates STOMP CONNECT with {@code Authorization: Bearer <jwt>}.
 * Keeps the JWT out of the WebSocket URL (query strings are often logged/proxied).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnect(accessor);
            return message;
        }

        if (requiresAuthenticatedUser(command) && accessor.getUser() == null) {
            throw new IllegalArgumentException("Unauthenticated STOMP session");
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String authorization = firstHeader(accessor, "Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Missing Authorization Bearer token");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Empty Authorization Bearer token");
        }

        try {
            if (jwtService.isTokenExpired(token)) {
                throw new IllegalArgumentException("Expired JWT");
            }
            String email = jwtService.extractUsername(token);
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Invalid JWT subject");
            }
            Principal user = () -> email;
            accessor.setUser(user);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("STOMP CONNECT JWT rejected: {}", ex.getMessage());
            throw new IllegalArgumentException("Invalid JWT");
        }
    }

    private static boolean requiresAuthenticatedUser(@Nullable StompCommand command) {
        return StompCommand.SEND.equals(command)
                || StompCommand.SUBSCRIBE.equals(command)
                || StompCommand.UNSUBSCRIBE.equals(command);
    }

    @Nullable
    private static String firstHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }
}
