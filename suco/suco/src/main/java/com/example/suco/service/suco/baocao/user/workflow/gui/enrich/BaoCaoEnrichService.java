package com.example.suco.service.suco.baocao.user.workflow.gui.enrich;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.service.location.GeocodingService;
import com.example.suco.service.suco.baocao.system.file.ImageStorageService;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import com.example.suco.service.suco.baocao.system.validation.TrungLapBaoCaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.model.enums.TrangThaiDuyet;

@Service
public class BaoCaoEnrichService {

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private TrungLapBaoCaoService trungLapBaoCaoService;

    @Autowired
    private BaoCaoRealtimeService realtimeService;

    @Autowired
    private SuCoMapper suCoMapper;

    public BaoCaoSuCo enrichAndSave(
            BaoCaoSuCo report,
            String base64
    ) {

        // =========================
        // GEO ENRICHMENT
        // =========================
        String address = geocodingService.getAddress(
                report.getViDo(),
                report.getKinhDo()
        );
        report.setDiaChi(address);

        // =========================
        // IMAGE ENRICHMENT
        // =========================
        if (base64 != null && !base64.isBlank()) {
            report.setHinhAnhUrl(
                    imageStorageService.saveBase64Image(base64)
            );
        }

        // =========================
        // AI INITIAL STATE (TYPE SAFE)
        // =========================
        report.setTrangThaiDuyet(TrangThaiDuyet.PENDING);
        report.setTrangThaiXuLy(TrangThaiXuLy.CHO_XU_LY);

        // =========================
        // TRUST DEFAULT (before AI)
        // =========================
        report.setDoTinCay(1);

        // =========================
        // SAVE FIRST
        // =========================
        BaoCaoSuCo saved = reportRepository.save(report);

        // =========================
        // DUPLICATE / AI SCORING
        // =========================
        trungLapBaoCaoService.recalculateTrust(saved);

        // nếu service có modify entity
        saved = reportRepository.save(saved);

        // =========================
        // REALTIME
        // =========================
        realtimeService.broadcastReport(
                suCoMapper.toMapDto(saved)
        );

        realtimeService.broadcastAdminNotification(
                "Có báo cáo mới chờ AI xử lý: " + saved.getMoTa()
        );

        return saved;
    }
}