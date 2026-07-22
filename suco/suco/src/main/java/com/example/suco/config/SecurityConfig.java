package com.example.suco.config;

import com.example.suco.security.FirebaseFilter;
import com.example.suco.security.JwtAuthenticationEntryPoint;
import com.example.suco.service.xacthuc.LogoutService;

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
import com.example.suco.repository.RefreshTokenRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class SecurityConfig {

    private final FirebaseFilter firebaseFilter;


    private final LogoutService logoutService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtDecoder jwtDecoder;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

public SecurityConfig(
        FirebaseFilter firebaseFilter,
        RefreshTokenRepository refreshTokenRepository,
        JwtDecoder jwtDecoder,
        LogoutService logoutService,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
) {

    this.firebaseFilter = firebaseFilter;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtDecoder = jwtDecoder;
    this.logoutService = logoutService;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
}
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {
        http
            // JWT stateless
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )
            .authorizeHttpRequests(auth -> auth


                // static
                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/favicon.ico"
                )
                .permitAll()



                // websocket
                .requestMatchers(
                        "/ws-suco/**",
                        "/ws-suco-web/**"
                )
                .permitAll()



                // auth
                .requestMatchers(
                        "/admin/login",
                        "/truso/login",
                        "/api/auth/**",
                        "/logout"
                )
                .permitAll()



                // public map
                .requestMatchers(
                        "/api/su-co/map",
                        "/api/sos/map"
                )
                .permitAll()



                // admin
                .requestMatchers(
                        "/admin/**"
                )
                .hasAnyAuthority(
                        "ROLE_ADMIN",
                        "SCOPE_ADMIN"
                )



                // api admin
                .requestMatchers(
                        "/api/admin/**",
                        "/api/goi/**"
                )
                .hasAnyAuthority(
                        "ROLE_ADMIN",
                        "SCOPE_ADMIN"
                )



                // tru so
                .requestMatchers(
                        "/truso/**"
                )
                .hasAnyAuthority(
                        "ROLE_TRUSO",
                        "SCOPE_TRUSO"
                )



                // user api
                .requestMatchers(
                        "/api/map/**",
                        "/api/qua/**",
                        "/api/su-co/**",
                        "/api/sos/**",
                        "/api/doi-tien/**",
                        "/api/quyen-gop/**"
                )
                .authenticated()



                .anyRequest()
                .authenticated()
            )




            .oauth2ResourceServer(oauth2 -> oauth2


                    /*
                     * Đọc JWT:
                     *
                     * 1. Browser:
                     * Cookie accessToken
                     *
                     * 2. Android:
                     * Authorization Bearer xxx
                     */
                    .bearerTokenResolver(request -> {


                        if (request.getCookies() != null) {


                            for (Cookie cookie :
                                    request.getCookies()) {


                                if ("accessToken"
                                        .equals(cookie.getName())) {


                                    return cookie.getValue();
                                }
                            }
                        }



                        return new DefaultBearerTokenResolver()
                                .resolve(request);
                    })



                    .jwt(jwt ->
                            jwt.jwtAuthenticationConverter(
                                    jwtAuthenticationConverter()
                            )
                    )


                    .authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )
            )




            .exceptionHandling(ex ->
                    ex.accessDeniedHandler(
                            new BearerTokenAccessDeniedHandler()
                    )
            )




            .logout(logout -> logout

        .logoutUrl("/logout")

        .clearAuthentication(true)


        .deleteCookies(
                "accessToken",
                "refreshToken"
        )


        .permitAll()


        .logoutSuccessHandler(
                (request, response, authentication) -> {


                    /*
                     * Xóa refresh token trong DB
                     */
                    Cookie[] cookies =
                            request.getCookies();


                    if (cookies != null) {

                        for (Cookie cookie : cookies) {


                            if ("refreshToken"
                                    .equals(cookie.getName())) {


                                try {


                                    Jwt jwt =
                                            jwtDecoder.decode(
                                                    cookie.getValue()
                                            );


                                    String jti =
                                            jwt.getId();


                                    if (jti != null) {

                                        logoutService.deleteRefreshToken(jti);
                                    }


                                } catch (Exception e) {

                                    System.out.println(
                                            "Không thể xóa refresh token DB: "
                                                    + e.getMessage()
                                    );
                                }


                                break;
                            }
                        }
                    }




                    response.setStatus(200);

                    response.setContentType(
                            "application/json"
                    );


                    response.getWriter()
                            .write("""
                            {
                              "message":"Logout success"
                            }
                            """);
                }
        )
);




        /*
         * Firebase cho Android
         *
         * Sau này sửa Android sang JWT
         * thì có thể bỏ filter này
         */
        http.addFilterBefore(
                firebaseFilter,
                UsernamePasswordAuthenticationFilter.class
        );



        return http.build();
    }






    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {


        JwtGrantedAuthoritiesConverter scopeConverter =
                new JwtGrantedAuthoritiesConverter();


        scopeConverter.setAuthoritiesClaimName(
                "scope"
        );


        scopeConverter.setAuthorityPrefix(
                "SCOPE_"
        );




        JwtGrantedAuthoritiesConverter roleConverter =
                new JwtGrantedAuthoritiesConverter();


        roleConverter.setAuthoritiesClaimName(
                "scope"
        );


        roleConverter.setAuthorityPrefix(
                "ROLE_"
        );




        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();



        converter.setJwtGrantedAuthoritiesConverter(jwt -> {


            var authorities =
                    scopeConverter.convert(jwt);



            authorities.addAll(
                    roleConverter.convert(jwt)
            );



            return authorities;
        });



        return converter;
    }
}