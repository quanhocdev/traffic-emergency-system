package com.example.suco.service.suco.baocao.user.workflow.gui;

import com.example.suco.model.BaoCaoSuCo;

import com.example.suco.service.suco.baocao.system.reward.UserRewardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaoCaoRewardService {

    @Autowired
    private UserRewardService userRewardService;

    public void rewardNewReport(
            String uid,
            BaoCaoSuCo report
    ) {

        if (report.getReporter() != null) {

            userRewardService.rewardUser(
                    uid,
                    5,
                    10
            );
        }
    }
}