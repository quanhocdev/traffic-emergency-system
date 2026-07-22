package com.example.suco.security;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;



@Component
public class JwtAuthenticationEntryPoint 
        implements AuthenticationEntryPoint {



    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {



        String path = request.getRequestURI();



        /*
         * Xử lý các trang Thymeleaf
         */
        if (path.startsWith("/admin")
                || path.startsWith("/truso")) {



            response.sendRedirect(
                    "/api/auth/refresh?redirect="
                    + path
            );


            return;
        }




        /*
         * API giữ nguyên 401
         */
        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED
        );

    }

}