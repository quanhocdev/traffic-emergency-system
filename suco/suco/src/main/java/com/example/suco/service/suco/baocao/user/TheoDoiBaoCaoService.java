package com.example.suco.service.suco.baocao.user;

import com.example.suco.dto.sos.tinhieu.UserInfoResponseDTO;
import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoItemResponseDTO;
import com.example.suco.dto.vanhanh.truso.TruSoMapDto;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.VipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TheoDoiBaoCaoService {

    @Autowired
    private BaoCaoSuCoRepository baoCaoSuCoRepository;

    @Autowired
    private SuCoMapper suCoMapper;

    @Autowired
    private VipService vipService;

    /**
     * 1. Lấy danh sách rút gọn (Item) dựa vào Firebase UID người dùng
     * Trả về List dùng cho màn hình danh sách lịch sử
     */
    public List<TheoDoiSuCoItemResponseDTO> layDanhSachItem(String uid) {
        return baoCaoSuCoRepository.findByReporterUid(uid)
                .stream()
                .map(suCo -> {
                    String tenTruSo = null;

                    if (suCo.getTruSoTiepNhan() != null) {
                        tenTruSo = suCo.getTruSoTiepNhan().getTenTruSo();
                    }

                    return suCoMapper.toTheoDoiItemDto(suCo, tenTruSo);
                })
                .toList();
    }

    /**
     * 2. Lấy thông tin CHI TIẾT của MỘT sự cố dựa vào ID
     * Trả về duy nhất 1 DTO đối tượng cụ thể (Giống hệt bên luồng SOS layChiTiet)
     */
    public TheoDoiSuCoDetailResponseDTO layChiTiet(Long id) {
        // Tìm sự cố cụ thể trong db, nếu không thấy thì báo lỗi
        var suCo = baoCaoSuCoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự cố với ID: " + id));

        // Map thông tin Trụ sở tiếp nhận từ thực thể liên kết @ManyToOne sang DTO bản đồ
        TruSoMapDto truSoDto = null;
        if (suCo.getTruSoTiepNhan() != null) {
            var truSo = suCo.getTruSoTiepNhan();
            truSoDto = new TruSoMapDto(
                    truSo.getId(),
                    truSo.getTenTruSo(),
                    truSo.getKinhDo(),
                    truSo.getViDo(),
                    truSo.getDiaChi()
            );
        }

        // Map thông tin tài khoản người báo cáo & kiểm tra gói VIP
        UserInfoResponseDTO userInfo = new UserInfoResponseDTO();
        if (suCo.getReporter() != null) {
            userInfo.setName(suCo.getReporter().getName());
            userInfo.setEmail(suCo.getReporter().getEmail());
            
            String reporterId = suCo.getReporter().getUid();
            userInfo.setVip(vipService.checkVip(reporterId));
        }

        // Chuyển đổi và trả về dữ liệu đơn lẻ
        return suCoMapper.toTheoDoiDto(suCo, truSoDto, userInfo);
    }
}