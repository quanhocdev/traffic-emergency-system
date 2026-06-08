package com.example.suco.service.suco.baocao.admin;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.LoaiSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.model.User;
import com.example.suco.model.enums.MucDoSuCo;
import com.example.suco.model.enums.TrangThaiDuyet;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.repository.suco.loai.LoaiSuCoRepository;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.repository.vanhanh.UserRepository;
import com.example.suco.service.suco.baocao.system.file.ImageStorageService;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import com.example.suco.service.dieuphoi.decision.TruSoSelectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.example.suco.service.location.GeocodingService;


@Service
public class AdminBaoCaoService {

    @Autowired
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoaiSuCoRepository loaiSuCoRepository;

    @Autowired
    private TruSoSelectorService truSoSelectorService;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private BaoCaoRealtimeService realtimeService;

    @Autowired
    private SuCoMapper suCoMapper;

    @Autowired
    private GeocodingService geocodingService;


    @Transactional
public BaoCaoSuCo submitAdminReport(BaoCaoSuCo report, MultipartFile image) {

    LoaiSuCo loaiSuCo = loaiSuCoRepository.findById(report.getLoaiSuCo().getId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Loại sự cố không tồn tại"));

    report.setLoaiSuCo(loaiSuCo);

    User adminUser = userRepository.findById("ADMIN_SYSTEM")
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ADMIN_SYSTEM"));

    report.setReporter(adminUser);
    report.setNguonBaoCao("ADMIN");

    report.setAiXacNhan(true);
    report.setTrangThaiDuyet(TrangThaiDuyet.VERIFIED);

    if (report.getMucDoSuCo() == null) {
        report.setMucDoSuCo(MucDoSuCo.NONE);
    }

    if (image != null && !image.isEmpty()) {
        report.setHinhAnhUrl(imageStorageService.saveMultipartImage(image));
    }

    // =========================
    // TRỤ SỞ GẦN NHẤT
    // =========================
    TruSo ganNhat = truSoSelectorService.selectNearest(
            report.getViDo(),
            report.getKinhDo()
    );

        report.setTruSoTiepNhan(ganNhat);
        report.setTrangThaiXuLy(TrangThaiXuLy.CHO_XU_LY);
    

    // =========================
    // SAVE FIRST
    // =========================
    BaoCaoSuCo savedReport = reportRepository.save(report);

    // =========================
    // GEO CODING (FIX ORDER BUG)
    // =========================
    if (savedReport.getDiaChi() == null || savedReport.getDiaChi().isBlank()) {
        String address = geocodingService.getAddress(
                savedReport.getViDo(),
                savedReport.getKinhDo()
        );

        savedReport.setDiaChi(address);
        savedReport = reportRepository.save(savedReport);
    }

    // =========================
    // REALTIME
    // =========================
    realtimeService.broadcastReport(
            suCoMapper.toMapDto(savedReport));

    if (ganNhat != null) {
        realtimeService.broadcastTruSo(
                ganNhat.getId(),
                suCoMapper.toMapDto(savedReport));
    }

    return savedReport;
}
}