package com.example.suco.controller.xacthuc;


import com.example.suco.service.xacthuc.RefreshTokenService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RefreshTokenController {



    private final RefreshTokenService refreshTokenService;



    @Value("${jwt.access-expiration}")
    private long accessExpirationMs;





    public RefreshTokenController(
            RefreshTokenService refreshTokenService
    ) {

        this.refreshTokenService = refreshTokenService;
    }







    @GetMapping("/refresh")
public void refreshPage(
        @CookieValue(
                name = "refreshToken",
                required = false
        )
        String refreshToken,

        @RequestParam("redirect")
        String redirect,

        HttpServletResponse response
) throws IOException {


    /*
     * Không có refresh token
     */
    if (refreshToken == null) {

        response.sendRedirect("/admin/login");

        return;
    }



    try {


        /*
         * Tạo access token mới
         */
        String newAccessToken =
                refreshTokenService.refreshAccessToken(
                        refreshToken
                );



        ResponseCookie accessCookie =
                ResponseCookie
                        .from(
                                "accessToken",
                                newAccessToken
                        )

                        .httpOnly(true)

                        .secure(false)

                        .path("/")

                        .sameSite("Lax")

                        .maxAge(
                                accessExpirationMs / 1000
                        )

                        .build();




        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessCookie.toString()
        );



        /*
         * quay lại trang người dùng đang mở
         */
        response.sendRedirect(
                redirect
        );


    } catch (Exception e) {


        /*
         * refresh token hết hạn
         */
        response.sendRedirect(
                "/admin/login"
        );

    }

}

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(
                    name = "refreshToken"
            )
            String refreshToken
    ) {



        try {


            String newAccessToken =
                    refreshTokenService
                            .refreshAccessToken(
                                    refreshToken
                            );





            ResponseCookie accessCookie =
                    ResponseCookie
                            .from(
                                    "accessToken",
                                    newAccessToken
                            )

                            .httpOnly(true)

                            .secure(false)

                            .path("/")

                            .sameSite("Lax")

                            .maxAge(
                                    accessExpirationMs / 1000
                            )

                            .build();





            return ResponseEntity.ok()

                    .header(
                            HttpHeaders.SET_COOKIE,
                            accessCookie.toString()
                    )

                    .body(
                            Map.of(
                                    "message",
                                    "Refresh token thành công"
                            )
                    );



        } catch (Exception e) {


            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }

    }

}