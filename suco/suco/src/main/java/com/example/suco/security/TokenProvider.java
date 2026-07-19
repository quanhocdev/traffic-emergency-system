package com.example.suco.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class TokenProvider {

    private final JwtEncoder jwtEncoder;
    private final long jwtExpirationMs;

    public TokenProvider(JwtEncoder jwtEncoder, 
                         @Value("${jwt.expiration}") long jwtExpirationMs) {
        this.jwtEncoder = jwtEncoder;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(String targetId, String role) {
        Instant now = Instant.now();
        long expirySeconds = jwtExpirationMs / 1000; 

        // [CẬP NHẬT] Định nghĩa Header chỉ định rõ thuật toán ký mã là HS256
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("suco-backend")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirySeconds))
                .subject(targetId)
                .claim("scope", role)
                .build();

        // [CẬP NHẬT] Gửi kèm cả JwsHeader vào để JwKEncoder không bị lỗi lạc lối tìm Key nữa
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}