package com.tripflow.chat.security;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * Turns handshake attribute userEmail into the STOMP Principal name.
 */
@Component
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        Object email = attributes.get(JwtHandshakeInterceptor.USER_EMAIL_ATTR);
        if (email instanceof String userEmail && !userEmail.isBlank()) {
            return () -> userEmail;
        }
        return null;
    }
}
