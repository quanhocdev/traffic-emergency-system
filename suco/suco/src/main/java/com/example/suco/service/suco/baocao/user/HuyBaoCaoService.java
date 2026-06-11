package com.example.suco.service.suco.baocao.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.dto.suco.baocao.SuCoMapResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.service.suco.baocao.system.validation.QuyenHanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HuyBaoCaoService {
    private static final Logger log = LoggerFactory.getLogger(HuyBaoCaoService.class);
    @Autowired
    private BaoCaoSuCoRepository reportRepository;
    @Autowired
    private QuyenHanService quyenHanService;
    @Autowired
    private SuCoMapper suCoMapper;
    @Autowired
    private BaoCaoRealtimeService realtimeService;


    private String maskUid(String uid) {

    if (uid == null) return "null";

    return uid.length() > 5
            ? uid.substring(0, 5) + "***"
            : uid + "***";
}
    
    @Transactional
public ResponseEntity<?> cancelReport(
        Long reportId,
        String currentUid
) {

    BaoCaoSuCo report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy báo cáo"
            ));

    log.info("\n[HUY BAO CAO] User: {} - Report: {}",
            maskUid(currentUid),
            reportId
    );

    // =========================
    // CHECK OWNER
    // =========================
    quyenHanService.checkOwner(report, currentUid);

    // =========================
    // BLOCK IF FINAL STATE
    // =========================
    if (report.getTrangThaiXuLy().isFinalState()) {

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "message",
                        "Báo cáo đã kết thúc, không thể hủy."
                ));
    }

    // =========================
    // ONLY USER CANCEL RULE
    // =========================
    if (!report.getTrangThaiXuLy().canBeCancelledByUser()) {

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "message",
                        "Không thể hủy báo cáo ở trạng thái hiện tại."
                ));
    }

    // =========================
    // UPDATE STATE
    // =========================
    report.setTrangThaiXuLy(TrangThaiXuLy.HUY_BO);

    BaoCaoSuCo saved = reportRepository.save(report);

    SuCoMapResponseDTO dto = suCoMapper.toMapDto(saved);

    realtimeService.broadcastReport(dto);

    realtimeService.refreshUserHistory(currentUid);

    log.info("\n[HUY BAO CAO SUCCESS] Report {}", reportId);

    return ResponseEntity.ok(
            Map.of("message", "Đã hủy báo cáo thành công")
    );
}

}
