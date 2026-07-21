package com.example.suco.service.xacthuc;

import com.example.suco.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CleanRefreshTokenService {


    private final RefreshTokenRepository refreshTokenRepository;


    public CleanRefreshTokenService(
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
    }



    /**
     * Tự động xóa refresh token đã hết hạn
     *
     * Chạy mỗi ngày lúc 2 giờ sáng
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredRefreshTokens() {


        refreshTokenRepository.deleteByExpiresAtBefore(
                Instant.now()
        );


        System.out.println(
                "Đã dọn refresh token hết hạn"
        );
    }

}