package com.example.suco.service.suco.baocao.user.workflow.gui.duplicate;

import com.example.suco.dto.BaoCaoResponse;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.User;

import com.example.suco.repository.UserRepository;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;

import com.example.suco.service.suco.baocao.system.builder.SuCoResponseBuilder;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import com.example.suco.service.suco.baocao.system.reward.UserRewardService;
import com.example.suco.service.suco.baocao.system.validation.TrungLapBaoCaoService;

import com.example.suco.service.suco.baocao.user.workflow.gui.response.BaoCaoResponseFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DuplicateBaoCaoService {

    @Autowired
    private TrungLapBaoCaoService trungLapBaoCaoService;

    @Autowired
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRewardService userRewardService;

    @Autowired
    private BaoCaoRealtimeService realtimeService;

    @Autowired
    private SuCoResponseBuilder suCoResponseBuilder;

    @Autowired
    private BaoCaoResponseFactory responseFactory;

    public BaoCaoResponse process(
            String uid,
            BaoCaoSuCo report
    ) {

        BaoCaoSuCo existingReport =
                trungLapBaoCaoService
                        .findDuplicateReport(report);

        if (existingReport == null) {
            return null;
        }

        if (existingReport.getReporter()
                .getUid()
                .equals(uid)) {

            return responseFactory.duplicate(
                    "Bạn đã báo cáo sự cố này trước đó",
                    existingReport.getDoTinCay()
            );
        }

        boolean existed =
                trungLapBaoCaoService
                        .isUserAlreadyContributed(
                                existingReport.getId(),
                                uid
                        );

        if (existed) {

            return responseFactory.duplicate(
                    "Bạn đã báo cáo sự cố này trước đó",
                    existingReport.getDoTinCay()
            );
        }

        trungLapBaoCaoService.saveDuplicateContributor(
                existingReport,
                uid
        );

        trungLapBaoCaoService.recalculateTrust(
                existingReport
        );

        reportRepository.save(existingReport);

        User user =
                userRepository.findById(uid)
                        .orElse(null);

        if (user != null) {

            userRewardService.rewardUser(
                    uid,
                    2,
                    5
            );
        }

        realtimeService.broadcastReport(
                suCoResponseBuilder
                        .buildSuCoDto(existingReport)
        );

        return responseFactory.duplicate(
                "Đã đóng góp vào báo cáo hiện có",
                existingReport.getDoTinCay()
        );
    }
}