package com.example.suco.service.xacthuc;


import com.example.suco.model.RefreshTokens;
import com.example.suco.repository.RefreshTokenRepository;
import com.example.suco.security.TokenProvider;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;


import java.time.Instant;



@Service
public class RefreshTokenService {


    private final RefreshTokenRepository refreshTokenRepository;

    private final TokenProvider tokenProvider;

    private final JwtDecoder jwtDecoder;



    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            TokenProvider tokenProvider,
            JwtDecoder jwtDecoder
    ) {

        this.refreshTokenRepository = refreshTokenRepository;

        this.tokenProvider = tokenProvider;

        this.jwtDecoder = jwtDecoder;
    }





    public String refreshAccessToken(
            String refreshTokenValue
    ) {



        /*
         * 1. Decode refresh token
         */
        Jwt jwt =
                jwtDecoder.decode(refreshTokenValue);




        /*
         * 2. Lấy jti
         */
        String jti =
                jwt.getId();




        if (jti == null) {

            throw new RuntimeException(
                    "Refresh token không có jti"
            );
        }





        /*
         * 3. Tìm refresh token trong DB
         */
        RefreshTokens refreshToken =
                refreshTokenRepository
                        .findByJti(jti)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Refresh token không tồn tại"
                                )
                        );





        /*
         * 4. Kiểm tra thời gian hết hạn
         */
        if (refreshToken.getExpiresAt()
                .isBefore(Instant.now())) {


            throw new RuntimeException(
                    "Refresh token đã hết hạn"
            );
        }





        /*
         * 5. Sinh access token mới
         */
        return tokenProvider.generateAccessToken(
                refreshToken.getAccountId(),
                refreshToken.getAccountType().name()
        );

    }
}