package com.example.suco.service.suco.baocao.admin;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.LoaiSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.model.User;
import com.example.suco.repository.LoaiSuCoRepository;
import com.example.suco.repository.UserRepository;
import com.example.suco.repository.BaoCaoSuCoRepository;
import com.example.suco.service.suco.baocao.system.file.ImageStorageService;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import com.example.suco.service.dieuphoi.decision.TruSoSelectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.example.suco.service.suco.baocao.system.builder.SuCoResponseBuilder;

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
    private SuCoResponseBuilder suCoResponseBuilder;


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
        report.setTrangThaiDuyet("VERIFIED");

        if (report.getMucDoNghiemTrong() == null) {
            report.setMucDoNghiemTrong("PENDING");
        }

        if (image != null && !image.isEmpty()) {
            report.setHinhAnhUrl(
                    imageStorageService.saveMultipartImage(image));
        }

        TruSo ganNhat = truSoSelectorService.selectNearest(
                report.getViDo(),
                report.getKinhDo());

        if (ganNhat != null) {
            report.setTruSoDeXuat(ganNhat);
            report.setTrangThaiXuLy("CHO_XU_LY");
        }

        BaoCaoSuCo savedReport = reportRepository.save(report);

        realtimeService.broadcastReport(
                suCoResponseBuilder.buildSuCoDto(savedReport));

        if (ganNhat != null) {
            realtimeService.broadcastTruSo(
                    ganNhat.getId(),
                    suCoResponseBuilder.buildSuCoDto(savedReport));
        }

        return savedReport;
    }
}