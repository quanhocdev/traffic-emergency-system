package com.example.suco.service.suco.baocao.user;
import com.example.suco.dto.suco.baocao.LichSuDto;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.HoaDon;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.sos.hoadon.HoaDonCuuHoRepository;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonUserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
public class LichSuService {

    @Autowired
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private TinHieuSOSRepository sosRepository;

    @Autowired
    private HoaDonCuuHoRepository hoaDonRepository;

    public List<LichSuDto> getAllHistory(String uid, String type) {

        List<LichSuDto> result = new ArrayList<>();

        // ===== SU CO =====
        if (type == null || type.equalsIgnoreCase("SU_CO")) {

            List<BaoCaoSuCo> suCoList = reportRepository.findByReporterUid(uid);

            for (BaoCaoSuCo sc : suCoList) {
                result.add(new LichSuDto(
                        sc.getId(),
                        "SU_CO",
                        sc.getLoaiSuCo() != null ? sc.getLoaiSuCo().getTen() : "Sự cố",
                        sc.getMoTa(),
                        sc.getTrangThaiXuLy(),
                        sc.getTrangThaiDuyet(),
                        sc.getHinhAnhUrl(),
                        sc.getViDo(),
                        sc.getKinhDo(),
                        null,
                        sc.getTruSoTiepNhan() != null ? "Đã tiếp nhận" : "Chờ xử lý",
                        sc.getThoiGianTao() != null ? sc.getThoiGianTao().toString() : "",
                        sc.getDiaChi(),
                        null
                ));
            }
        }

        // ===== SOS =====
        if (type == null || type.equalsIgnoreCase("SOS")) {

            List<TinHieuSOS> sosList = sosRepository.findByUserUid(uid);

            for (TinHieuSOS s : sosList) {

                HoaDonUserResponseDTO hd = hoaDonRepository
                        .findFirstBySosIdOrderByIdDesc(s.getId())
                        .orElse(null);

                result.add(new LichSuDto(
                        s.getId(),
                        "SOS",
                        "Yêu cầu cứu hộ khẩn cấp",
                        s.getGhiChu(),
                        s.getTrangThai(),
                        "VERIFIED",
                        s.getHinhAnh(),
                        s.getViDo(),
                        s.getKinhDo(),
                        s.getGhiAm(),
                        s.getIdTruSoTiepNhan() != null ? "Đang xử lý" : "Chờ điều phối",
                        s.getCreatedAt() != null ? s.getCreatedAt().toString() : "",
                        s.getDiaChi(),
                        hd
                ));
            }
        }

        result.sort((a, b) -> b.getId().compareTo(a.getId()));
        return result;
    }
}