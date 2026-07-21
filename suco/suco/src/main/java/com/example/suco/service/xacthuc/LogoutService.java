package com.example.suco.service.xacthuc;


import com.example.suco.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class LogoutService {


    private final RefreshTokenRepository refreshTokenRepository;


    public LogoutService(
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
    }



    @Transactional
    public void deleteRefreshToken(String jti) {

        refreshTokenRepository.deleteByJti(jti);

    }

}