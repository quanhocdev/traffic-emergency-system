package com.example.suco.service.suco.baocao.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import com.example.suco.dto.SuCoMapDto;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.service.LichSuService;
import com.example.suco.service.suco.baocao.system.validation.QuyenHanService;
import com.example.suco.service.suco.baocao.system.builder.SuCoResponseBuilder;
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
    private SuCoResponseBuilder suCoResponseBuilder;
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

        BaoCaoSuCo report =
                reportRepository.findById(reportId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Không tìm thấy báo cáo"
                                ));

        log.info(
                "\n[HUY BAO CAO - BẮT ĐẦU]"
                        + "\nUser: {}"
                        + "\nReport ID: {}"
                        + "\nTrang thai xu ly: {}"
                        + "\nTrang thai duyet: {}\n",

                maskUid(currentUid),
                reportId,
                report.getTrangThaiXuLy(),
                report.getTrangThaiDuyet()
        );

        quyenHanService.checkOwner(
                report,
                currentUid
        );

        String xuLy =
                report.getTrangThaiXuLy();

        String duyet =
                report.getTrangThaiDuyet();

        if ("VERIFIED".equals(duyet)) {

            log.warn(
                    "\n[HUY BAO CAO - BỊ CHẶN]"
                            + "\nUser: {}"
                            + "\nReport ID: {}"
                            + "\nLý do: Đã VERIFIED\n",

                    maskUid(currentUid),
                    reportId
            );

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Báo cáo đã được duyệt, không thể hủy."
                            )
                    );
        }

        if (!"CHO_XU_LY".equals(xuLy)) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Không thể hủy báo cáo ở trạng thái hiện tại."
                            )
                    );
        }

        report.setTrangThaiXuLy("HUY_BO");

        BaoCaoSuCo saved =
                reportRepository.save(report);

        SuCoMapDto dto =
                suCoResponseBuilder.buildSuCoDto(saved);

        dto.setDiaChi(saved.getDiaChi());

        realtimeService.broadcastReport(dto);

        realtimeService.refreshUserHistory(
                currentUid
        );

        log.info(
                "\n[HUY BAO CAO - THÀNH CÔNG]"
                        + "\nUser: {}"
                        + "\nReport ID: {}"
                        + "\nXu ly cu: {}"
                        + "\nDuyet: {}"
                        + "\nXu ly moi: HUY_BO\n",

                maskUid(currentUid),
                reportId,
                xuLy,
                duyet
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Đã hủy báo cáo thành công"
                )
        );
    }

}
