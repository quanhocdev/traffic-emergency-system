package com.example.suco.security;

import com.example.suco.model.TruSo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        System.out.println("========== WS CONNECT ==========");
        System.out.println("Headers : " + accessor.toNativeHeaderMap());

        Map<String, Object> attrs = accessor.getSessionAttributes();

        System.out.println("Session attrs = " + attrs);

if (attrs != null) {
    System.out.println("Keys = " + attrs.keySet());
}

        /*
         * ==========================================================
         * TRỤ SỞ (SESSION)
         * ==========================================================
         */
        if (attrs != null) {

            Object obj = attrs.get("currentTruSo");

            if (obj instanceof TruSo truSo) {

                System.out.println("WS Login by SESSION");
                System.out.println("TruSo = " + truSo.getTenTruSo());

                accessor.setUser(new Principal() {
                    @Override
                    public String getName() {
                        return String.valueOf(truSo.getId());
                    }
                });

                return message;
            }
        }

        /*
         * ==========================================================
         * JWT (ANDROID / ADMIN)
         * ==========================================================
         */

        String token = null;

        // Android
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // Admin Cookie
        if (token == null && attrs != null) {

            Object cookieObj = attrs.get("cookie");

            if (cookieObj instanceof Cookie[] cookies) {

                for (Cookie cookie : cookies) {

                    if ("ADMIN_JWT".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Không tìm thấy JWT hoặc Session.");
        }

        Claims claims = jwtService.extractAllClaims(token);

        String principalId = claims.getSubject();

        accessor.setUser(() -> principalId);

        System.out.println("WS Login by JWT : " + principalId);

        return message;
    }
}