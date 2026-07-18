package com.example.suco.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class TokenProvider {

    private final JwtEncoder jwtEncoder;
    private final long jwtExpirationMs;

    // Inject giá trị cấu hình vào qua Constructor
    public TokenProvider(JwtEncoder jwtEncoder, 
                         @Value("${jwt.expiration}") long jwtExpirationMs) {
        this.jwtEncoder = jwtEncoder;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(String targetId, String role) {
        Instant now = Instant.now();
        // jwt.expiration cấu hình ở dự án là dạng Miliseconds, chuyển đổi sang Giây
        long expirySeconds = jwtExpirationMs / 1000; 

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("suco-backend")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirySeconds))
                .subject(targetId)
                .claim("scope", role)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}