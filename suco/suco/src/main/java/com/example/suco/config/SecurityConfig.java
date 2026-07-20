package com.example.suco.config;

import com.example.suco.security.FirebaseFilter;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final FirebaseFilter firebaseFilter;

    public SecurityConfig(FirebaseFilter firebaseFilter) {
        this.firebaseFilter = firebaseFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // 1. Cho phép tài nguyên tĩnh công khai công cộng
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico").permitAll()
                .requestMatchers("/ws-suco/**", "/ws-suco-web/**").permitAll()
                
                // 2. Cho phép các trang login truy cập tự do
                .requestMatchers("/admin/login", "/truso/login", "/logout", "/api/auth/**").permitAll()
                
                // Bản đồ công khai
                .requestMatchers("/api/su-co/map", "/api/sos/map").permitAll()

                // 3. Phân quyền cụ thể dựa trên SCOPE (Từ JWT của Trụ sở) và ROLE (Từ Admin)
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SCOPE_ADMIN")
                .requestMatchers("/api/goi/**", "/api/admin/**").hasAnyAuthority("SCOPE_ADMIN", "ROLE_ADMIN")
                
                // Phân vùng dành riêng cho trụ sở
                .requestMatchers("/truso/**").hasAnyAuthority("SCOPE_TRUSO", "ROLE_TRUSO")

                // 4. Tất cả các api vận hành còn lại yêu cầu phải xác thực
                .requestMatchers("/api/map/**", "/api/qua/**", "/api/su-co/**", 
                                 "/api/sos/**", "/api/doi-tien/**", "/api/quyen-gop/**").authenticated()

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(request -> {
                    if (request.getCookies() != null) {
                        for (Cookie cookie : request.getCookies()) {
                            if ("accessToken".equals(cookie.getName())) {
                                return cookie.getValue();
                            }
                        }
                    }
                    return new DefaultBearerTokenResolver().resolve(request);
                })
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .deleteCookies("accessToken")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .permitAll()
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write("""
                        {"message":"Logout success"}
                    """);
                })
            );

        // Đặt FirebaseFilter chạy trước để gánh phần xác thực Admin Firebase
        http.addFilterBefore(firebaseFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthoritiesClaimName("scope");
        scopeConverter.setAuthorityPrefix("SCOPE_");

        JwtGrantedAuthoritiesConverter roleConverter = new JwtGrantedAuthoritiesConverter();
        roleConverter.setAuthoritiesClaimName("scope");
        roleConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = scopeConverter.convert(jwt);
            authorities.addAll(roleConverter.convert(jwt));
            return authorities;
        });
        return converter;
    }
}