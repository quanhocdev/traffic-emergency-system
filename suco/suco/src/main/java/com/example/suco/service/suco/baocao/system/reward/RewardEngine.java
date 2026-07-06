package com.example.suco.service.suco.baocao.system.reward;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RewardEngine {

    @Autowired
    private UserRewardService userRewardService;

    public void reward(String uid, BaoCaoRewardPolicy policy) {

        userRewardService.rewardUser(
                uid,
                policy.normalPoint(),
                policy.vipPoint()
        );
    }
}