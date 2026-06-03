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

        report.setDiaChi(
                geocodingService.getAddress(
                        report.getViDo(),
                        report.getKinhDo()
                )
        );

        if (base64 != null
                && !base64.isBlank()) {

            report.setHinhAnhUrl(
                    imageStorageService
                            .saveBase64Image(base64)
            );
        }

        report.setTrangThaiDuyet("AI_APPROVED");
        report.setTrangThaiXuLy("CHO_XU_LY");
        report.setDoTinCay(0);

        report.setTruSoDeXuat(null);
        report.setTruSoTiepNhan(null);

        BaoCaoSuCo saved =
                reportRepository.save(report);

        trungLapBaoCaoService
                .recalculateTrust(saved);

        saved =
                reportRepository.save(saved);

        realtimeService.broadcastReport(
                suCoMapper
                        .toMapDto(saved)
        );

        realtimeService.broadcastAdminNotification(
                "Có báo cáo mới chờ duyệt: "
                        + saved.getMoTa()
        );

        return saved;
    }
}