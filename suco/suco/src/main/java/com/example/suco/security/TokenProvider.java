package com.example.suco.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class TokenProvider {


    private final JwtEncoder jwtEncoder;

    private final long accessExpirationMs;

    private final long refreshExpirationMs;


    public TokenProvider(
            JwtEncoder jwtEncoder,

            @Value("${jwt.access-expiration}")
            long accessExpirationMs,

            @Value("${jwt.refresh-expiration}")
            long refreshExpirationMs
    ) {

        this.jwtEncoder = jwtEncoder;

        this.accessExpirationMs = accessExpirationMs;

        this.refreshExpirationMs = refreshExpirationMs;
    }



    /**
     * Access Token
     *
     * Dùng gọi API
     *
     * Sống ngắn
     */
    public String generateAccessToken(
            String targetId,
            String role
    ) {

        Instant now = Instant.now();


        JwsHeader header =
                JwsHeader.with(MacAlgorithm.HS256)
                        .build();


        JwtClaimsSet claims =
                JwtClaimsSet.builder()

                .issuer("suco-backend")

                .issuedAt(now)

                .expiresAt(
                    now.plusMillis(accessExpirationMs)
                )

                .subject(targetId)

                .claim("scope", role)

                .claim("type", "ACCESS")

                .build();

        return jwtEncoder
                .encode(
                    JwtEncoderParameters.from(
                        header,
                        claims
                    )
                )
                .getTokenValue();
    }

    /**
     * Refresh Token
     *
     * Dùng lấy Access Token mới
     *
     * Có jti để lưu DB
     */
    public RefreshTokenInfo generateRefreshToken(
            String targetId,
            String accountType
    ) {
        Instant now = Instant.now();
        String jti =
                UUID.randomUUID().toString();

        JwsHeader header =
                JwsHeader.with(MacAlgorithm.HS256)
                        .build();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                .issuer("suco-backend")
                .issuedAt(now)
                .expiresAt(
                    now.plusMillis(refreshExpirationMs)
                )
                .subject(targetId)
                .id(jti)
                .claim("type", "REFRESH")
                .claim("accountType", accountType)
                .build();

        String token =
                jwtEncoder
                .encode(
                    JwtEncoderParameters.from(
                        header,
                        claims
                    )
                )
                .getTokenValue();

        return new RefreshTokenInfo(
                token,
                jti,
                now.plusMillis(refreshExpirationMs)
        );
    }

    public record RefreshTokenInfo(
            String token,
            String jti,
            Instant expiresAt
    ) {}

}