package com.example.suco.service.suco.baocao.admin;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.Spam;
import com.example.suco.model.TruSo;
import com.example.suco.model.User;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.repository.suco.baocao.SpamRepository;
import com.example.suco.repository.vanhanh.UserRepository;
import com.example.suco.service.dieuphoi.decision.TruSoSelectorService;
import com.example.suco.dto.suco.baocao.AdminSuCoDetailResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import com.example.suco.service.suco.baocao.system.reward.UserRewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DuyetSuCoService {

    @Autowired
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpamRepository spamRepository;

    @Autowired
    private TruSoSelectorService truSoSelectorService;

    @Autowired
    private UserRewardService userRewardService;

    @Autowired
    private BaoCaoRealtimeService realtimeService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
        private SuCoMapper suCoMapper;

    private static final Logger log =
        LoggerFactory.getLogger(DuyetSuCoService.class);

    public List<AdminSuCoDetailResponseDTO> getPendingReportsForAdmin() {

    List<BaoCaoSuCo> reports = reportRepository.findPendingReportsForAdmin();

    log.info("Đang tải {} báo cáo chờ duyệt", reports.size());

    return reports.stream()
            .map(suCoMapper::toAdminDetailDto)
            .toList();
}

    @Transactional
    public void verifyReport(Long reportId, boolean isCorrect) {

        BaoCaoSuCo report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy báo cáo"
                ));

        User reporter = report.getReporter();

        if (isCorrect) {

            report.setTrangThaiDuyet("VERIFIED");

            if (reporter != null && !"ADMIN".equals(report.getNguonBaoCao())) {

                int pointsToAdd =
                        userRewardService.isUserVip(reporter.getUid()) ? 10 : 5;

                reporter.setTotalPoints(
                        reporter.getTotalPoints() + pointsToAdd);

                userRepository.save(reporter);

                messagingTemplate.convertAndSend(
                        "/topic/user-stats/" + reporter.getUid(),
                        reporter
                );
            }

            TruSo ganNhat = truSoSelectorService.selectNearest(
                    report.getViDo(),
                    report.getKinhDo());

            if (ganNhat != null) {
                report.setTruSoDeXuat(ganNhat);
            }

            BaoCaoSuCo updatedReport = reportRepository.save(report);

            realtimeService.broadcastReport(
        suCoMapper.toMapDto(updatedReport));

            if (updatedReport.getTruSoDeXuat() != null) {

                realtimeService.broadcastTruSo(
        updatedReport.getTruSoDeXuat().getId(),
        suCoMapper.toMapDto(updatedReport)
);
            }

            if (updatedReport.getTruSoTiepNhan() != null) {

                realtimeService.broadcastTruSo(
        updatedReport.getTruSoDeXuat().getId(),
        suCoMapper.toMapDto(updatedReport)
);
            }

        } else {

            Spam spam = new Spam(report);
            spamRepository.save(spam);

            if (reporter != null && !"ADMIN".equals(report.getNguonBaoCao())) {

                reporter.setSpamCount(
                        reporter.getSpamCount() + 1);

                userRepository.save(reporter);

                messagingTemplate.convertAndSend(
                        "/topic/user-stats/" + reporter.getUid(),
                        reporter
                );
            }

            report.setTrangThaiDuyet("REJECTED");
            report.setTrangThaiXuLy("REJECTED");

            realtimeService.broadcastReport(
                    suCoMapper.toMapDto(report));

            reportRepository.delete(report);

            realtimeService.broadcastDelete(reportId);

            return;
        }
    }
    
}