package com.example.suco.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import com.example.suco.service.xacthuc.user.token.FirebaseService;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
            
    @Autowired
    private final FirebaseService firebaseService;

    @Autowired
    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(FirebaseService firebaseService, JwtDecoder jwtDecoder) {
        this.firebaseService = firebaseService;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {

            System.out.println("========== WS CONNECT ==========");
            System.out.println("Headers : " + accessor.toNativeHeaderMap());

            /*
             * 1. XÁC THỰC TRỤ SỞ QUA JWT LƯU TRONG COOKIE (THAY THẾ SESSION)
             */
            String token = null;
            String cookieHeader = accessor.getFirstNativeHeader("Cookie");
            
            if (cookieHeader != null) {
                // Phân tích chuỗi Cookie để bóc tách lấy giá trị của accessToken
                for (String cookie : cookieHeader.split(";")) {
                    String[] pair = cookie.trim().split("=");
                    if (pair.length == 2 && "accessToken".equals(pair[0])) {
                        token = pair[1];
                        break;
                    }
                }
            }

            if (token != null) {
                try {
                    // Giải mã chuỗi JWT nhận được
                    var jwt = jwtDecoder.decode(token);
                    String scope = jwt.getClaimAsString("scope");
                    
                    // Nếu đúng vai trò TRUSO, gán Principal định danh thông qua ID Trụ sở
                    if ("TRUSO".equals(scope)) {
                        String truSoId = jwt.getSubject();
                        accessor.setUser(() -> truSoId);
                        
                        System.out.println("WS Login via JWT Cookie for TruSo ID: " + truSoId);
                        return message;
                    }
                } catch (Exception e) {
                    log.error("JWT WebSocket verification failed", e);
                }
            }

            /*
             * 2. XÁC THỰC USER THIẾT BỊ DI ĐỘNG QUA FIREBASE (GIỮ NGUYÊN)
             */
            String header = accessor.getFirstNativeHeader("Authorization");
            log.info("STEP 1 - Authorization header exists: {}", header != null);

            if (header != null && header.startsWith("Bearer ")) {
                log.info("STEP 2 - Firebase verify");
                String uid;
                try {
                    uid = firebaseService.extractUid(header);
                    log.info("STEP 3 - Firebase UID = {}", uid);
                } catch (Exception e) {
                    log.error("Firebase verify failed", e);
                    throw e;
                }
                accessor.setUser(() -> uid);
                log.info("STEP 4 - User set");
            }
        }

        return message;
    }
}