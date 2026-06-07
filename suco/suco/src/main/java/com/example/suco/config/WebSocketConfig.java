package com.example.suco.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Cấu hình bộ đếm thời gian Heartbeat độc lập để tránh nghẽn luồng
        ThreadPoolTaskScheduler te = new ThreadPoolTaskScheduler();
        te.setPoolSize(1);
        te.setThreadNamePrefix("ws-heartbeat-thread-");
        te.initialize();

        // Kích hoạt broker với nhịp tim giữ kết nối (Ping/Pong)
        // 20000, 20000: Cứ 20 giây Server và Client sẽ gửi tín hiệu kiểm tra nhau một lần để giữ đường truyền luôn SỐNG
        config.enableSimpleBroker("/topic")
              .setHeartbeatValue(new long[]{20000, 20000})
              .setTaskScheduler(te);
              
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 💡 GIẢI PHÁP CHÍ MẠNG: Đăng ký 2 Endpoint tách biệt
        
        // Endpoint 1: Dành riêng cho Android (Không có .withSockJS() để chạy WebSocket thuần siêu ổn định)
        registry.addEndpoint("/ws-suco")
                .setAllowedOriginPatterns("*");

        // Endpoint 2: Dành cho Web (Giữ lại .withSockJS() nếu bạn có làm giao diện Web admin)
        registry.addEndpoint("/ws-suco-web")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}