package com.example.suco.config;

import com.example.suco.security.CustomAuthEntryPoint;
import com.example.suco.security.FirebaseFilter;
import com.example.suco.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final FirebaseFilter firebaseFilter;
    private final JwtFilter jwtFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;

    public SecurityConfig(FirebaseFilter firebaseFilter,
                      JwtFilter jwtFilter,
                      CustomAuthEntryPoint customAuthEntryPoint) {
    this.firebaseFilter = firebaseFilter;
    this.jwtFilter = jwtFilter;
    this.customAuthEntryPoint = customAuthEntryPoint;
}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            // Session
            .securityContext(context -> context
        .requireExplicitSave(false)
    )
            .exceptionHandling(ex -> ex
            .authenticationEntryPoint(customAuthEntryPoint)
        )
            .authorizeHttpRequests(auth -> auth

            .requestMatchers("/ws-suco/**", "/ws-suco-web/**").permitAll()
            
    // PUBLIC
    .requestMatchers("/admin/login").permitAll()
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/su-co/map", "/api/sos/map").permitAll()

    // ADMIN ONLY
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/goi/**").hasRole("ADMIN")

    // AUTH SYNC USER
    .requestMatchers("/api/map/**").authenticated()

    .anyRequest().permitAll()
)

            // JWT ADMIN chạy trước
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // Firebase USER chạy sau
            .addFilterBefore(firebaseFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}