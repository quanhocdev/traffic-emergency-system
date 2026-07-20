package com.example.suco.repository;

import com.example.suco.model.RefreshTokens;
import com.example.suco.model.enums.RefreshTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository 
        extends JpaRepository<RefreshTokens, Long> {


    Optional<RefreshTokens> findByJti(String jti);


    void deleteByJti(String jti);


    void deleteByAccountIdAndAccountType(
            String accountId,
            RefreshTokenType accountType
    );
}