package com.example.suco.service.suco.baocao.system.reward;

import com.example.suco.model.User;
import com.example.suco.repository.UserRepository;
import com.example.suco.repository.sos.goi.SoHuuGoiRepository;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserRewardService {

    @Autowired
    private SoHuuGoiRepository muaGoiRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BaoCaoRealtimeService realtimeService;

    public boolean isUserVip(String userId) {

        return muaGoiRepository
                .findFirstByUserIdAndTrangThaiAndNgayHetHanAfter(
                        userId,
                        "ACTIVE",
                        LocalDateTime.now()
                )
                .isPresent();
    }

    public void rewardUser(String uid, int normalPoints, int vipPoints) {

        User user = userRepository.findById(uid).orElse(null);

        if (user == null) {
            return;
        }

        int pointsToAdd =
                isUserVip(uid)
                        ? vipPoints
                        : normalPoints;

        user.setTotalPoints(
                user.getTotalPoints() + pointsToAdd
        );

        userRepository.save(user);

        realtimeService.broadcastUserStats(
                uid,
                user
        );
    }

    public void increaseSpamCount(String uid) {

        User user = userRepository.findById(uid).orElse(null);

        if (user == null) {
            return;
        }

        user.setSpamCount(
                user.getSpamCount() + 1
        );

        userRepository.save(user);

        realtimeService.broadcastUserStats(
                uid,
                user
        );
    }
}