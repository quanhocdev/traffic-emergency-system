package com.example.suco.service.suco.baocao.admin;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.LoaiSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.model.User;
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

        // Tìm trụ sở gần nhất để tiếp nhận
        TruSo ganNhat = truSoSelectorService.selectNearest(
        report.getViDo(),
        report.getKinhDo());

if (ganNhat != null) {

    report.setTruSoTiepNhan(ganNhat);

    report.setTrangThaiXuLy("DANG_XU_LY");

} else {

    report.setTrangThaiXuLy("CHO_ADMIN");
}
    
// Lưu báo cáo vào database
        BaoCaoSuCo savedReport = reportRepository.save(report);

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