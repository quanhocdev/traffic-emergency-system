package com.example.suco.model;

import com.example.suco.model.enums.RefreshTokenType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // JWT ID trong refresh token
    @Column(nullable = false, unique = true, length = 100)
    private String jti;


    // 
    // USER  -> users.uid
    // TRUSO -> truso.id
    //
    @Column(nullable = false)
    private String accountId;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefreshTokenType accountType;


    @Column(nullable = false)
    private Instant expiresAt;


    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    public RefreshTokens() {
    }


    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }


    public Long getId() {
        return id;
    }


    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }


    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }


    public RefreshTokenType getAccountType() {
        return accountType;
    }

    public void setAccountType(RefreshTokenType accountType) {
        this.accountType = accountType;
    }


    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }




    public Instant getCreatedAt() {
        return createdAt;
    }
}