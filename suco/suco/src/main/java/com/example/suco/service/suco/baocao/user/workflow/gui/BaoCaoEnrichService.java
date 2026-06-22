package com.example.suco.service.suco.baocao.user.workflow.gui;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.repository.suco.baocao.SuCoAdminRepository;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.service.location.GeocodingService;
import com.example.suco.service.suco.baocao.system.file.ImageStorageService;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import com.example.suco.service.suco.baocao.system.validation.TrungLapBaoCaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.model.enums.TrangThaiXuLy;

@Service
public class BaoCaoEnrichService {

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private SuCoAdminRepository reportRepository;

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


        String address = geocodingService.getAddress(
                report.getViDo(),
                report.getKinhDo()
        );
        report.setDiaChi(address);

        // kiểm tra ảnh có hợp lệ không, nếu có thì lưu và set URL
        if (base64 != null && !base64.isBlank()) {
            report.setHinhAnhUrl(
                    imageStorageService.saveBase64Image(base64)
            );
        }

        report.setDoTinCay(1);

        BaoCaoSuCo saved = reportRepository.save(report);

        trungLapBaoCaoService.recalculateTrust(saved);

        saved = reportRepository.save(saved);

        realtimeService.broadcastReport(
                suCoMapper.toMapDto(saved)
        );

        realtimeService.broadcastAdminNotification(
                "Có báo cáo mới chờ AI xử lý: " + saved.getMoTa()
        );

        return saved;
    }
}