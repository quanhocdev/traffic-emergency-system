package com.example.suco.config;

import com.example.suco.security.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    // Tiêm bộ lọc bảo mật vào cấu hình
    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        ThreadPoolTaskScheduler te = new ThreadPoolTaskScheduler();
        te.setPoolSize(1);
        te.setThreadNamePrefix("ws-heartbeat-thread-");
        te.initialize();

        // Thêm cấu hình Broker
        config.enableSimpleBroker("/topic", "/queue"); // Thêm "/queue" phục vụ gửi tin nhắn riêng tư
        config.setApplicationDestinationPrefixes("/app");
        
        // Bật tiền tố định tuyến cá nhân: Mặc định Spring sẽ map các kênh "/user/..." thành kênh riêng biệt
        config.setUserDestinationPrefix("/user");

        config.enableSimpleBroker("/topic", "/queue")
              .setHeartbeatValue(new long[]{20000, 20000})
              .setTaskScheduler(te);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-suco")
                .setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws-suco-web")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // Đăng ký Interceptor bảo mật vào luồng xử lý tin nhắn đến từ Client
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}