package com.example.suco.security;

import com.example.suco.service.xacthuc.user.token.FirebaseService;
import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final FirebaseService firebaseService; // Inject Service của bạn vào đây

    public WebSocketAuthInterceptor(JwtService jwtService, FirebaseService firebaseService) {
        this.jwtService = jwtService;
        this.firebaseService = firebaseService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ kiểm tra khi Client gửi lệnh CONNECT
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    UsernamePasswordAuthenticationToken authentication;

                    // 1. Kiểm tra nếu là JWT của ADMIN (Token có 3 phần ngăn cách bởi dấu chấm)
                    if (token.split("\\.").length == 3) {
                        Claims claims = jwtService.extractAllClaims(token);
                        String role = (String) claims.get("role");
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                        
                        authentication = new UsernamePasswordAuthenticationToken(
                                claims.getSubject(), null, authorities
                        );
                    } else { 
                        // 2. TÁI SỬ DỤNG: Dùng chính FirebaseService của bạn để bóc UID cho USER
                        String uid = firebaseService.extractUid(authHeader);
                        
                        authentication = new UsernamePasswordAuthenticationToken(
                                uid, null, List.of()
                        );
                    }

                    // Lưu thông tin định danh vào phiên kết nối của Socket này
                    accessor.setUser(authentication);

                } catch (Exception e) {
                    throw new IllegalArgumentException("Xác thực WebSocket thất bại: " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("Thiếu Token xác thực khi kết nối WebSocket.");
            }
        }
        return message;
    }
}