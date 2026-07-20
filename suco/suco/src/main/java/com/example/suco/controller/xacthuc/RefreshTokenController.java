package com.example.suco.controller.xacthuc;


import com.example.suco.service.xacthuc.RefreshTokenService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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