    package com.example.suco.config;

    import com.example.suco.security.WebSocketAuthInterceptor;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.messaging.simp.config.ChannelRegistration;
    import org.springframework.messaging.simp.config.MessageBrokerRegistry;
    import org.springframework.web.socket.config.annotation.*;
    import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

    @Configuration
    @EnableWebSocketMessageBroker
    public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

        private final WebSocketAuthInterceptor webSocketAuthInterceptor;

        public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
            this.webSocketAuthInterceptor = webSocketAuthInterceptor;
        }

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {

            config.enableSimpleBroker("/topic", "/queue");

            config.setApplicationDestinationPrefixes("/app");
            config.setUserDestinationPrefix("/user");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            // Endpoint cho mobile (App Android / iOS)
            registry.addEndpoint("/ws-suco")
                    .setAllowedOriginPatterns("*");

            // Endpoint cho Web Trụ sở / Admin
            registry.addEndpoint("/ws-suco-web")
                    .setAllowedOriginPatterns("*")
                    .addInterceptors(new HttpSessionHandshakeInterceptor()) // 🌟 Bắt Cookie đưa vào Session Attributes
                    .withSockJS();
        }

        @Override
        public void configureClientInboundChannel(ChannelRegistration registration) {
            registration.interceptors(webSocketAuthInterceptor);
        }
    }