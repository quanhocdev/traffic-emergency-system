package com.example.suco.config;


import com.example.suco.security.WebSocketAuthInterceptor;

import org.springframework.context.annotation.Configuration;

import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

import org.springframework.web.socket.config.annotation.*;



@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {



    private final WebSocketAuthInterceptor webSocketAuthInterceptor;



    public WebSocketConfig(
            WebSocketAuthInterceptor webSocketAuthInterceptor
    ) {

        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }





    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry config
    ) {


        config.enableSimpleBroker(
                "/topic",
                "/queue"
        );


        config.setApplicationDestinationPrefixes(
                "/app"
        );


        config.setUserDestinationPrefix(
                "/user"
        );

    }







    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {


        /*
         * Android
         *
         * Sau này dùng:
         * Authorization: Bearer Firebase/JWT
         */
        registry.addEndpoint(
                "/ws-suco"
        )
        .setAllowedOriginPatterns("*");






        /*
         * Web Admin + Trụ sở
         *
         * JWT nằm trong HttpOnly Cookie
         *
         * WebSocketAuthInterceptor
         * sẽ đọc Cookie accessToken
         */
        registry.addEndpoint(
                "/ws-suco-web"
        )
        .setAllowedOriginPatterns("*")
        .withSockJS();

    }







    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {

        registration.interceptors(
                webSocketAuthInterceptor
        );

    }

}