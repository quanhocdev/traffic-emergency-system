package com.example.suco.service.suco.baocao.user;

import com.example.suco.dto.info.truso.TruSoMapDto;
import com.example.suco.dto.info.user.UserInfoResponseDTO;
import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoItemResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.repository.suco.baocao.SuCoAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.suco.mapper.info.InfoTruSoMapper;
import com.example.suco.mapper.info.InfoUserMapper;

@Service
public class TheoDoiBaoCaoService {

    @Autowired
    private SuCoAdminRepository baoCaoSuCoRepository;

    @Autowired
    private SuCoMapper suCoMapper;

    @Autowired
    private InfoTruSoMapper infoTruSoMapper;

    @Autowired
    private InfoUserMapper infoUserMapper;

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

    public TheoDoiSuCoDetailResponseDTO layChiTiet(Long id, String uid) {
        
        var suCo = baoCaoSuCoRepository
        .findByIdAndReporterUid(id, uid)
        .orElseThrow(() ->
                new RuntimeException("Không tìm thấy sự cố với ID: " + id));

        // Map thông tin Trụ sở tiếp nhận từ thực thể liên kết @ManyToOne sang DTO bản đồ
        TruSoMapDto truSoDto = infoTruSoMapper.toMapDto(suCo.getTruSoTiepNhan());

        // Map thông tin tài khoản người báo cáo & kiểm tra gói VIP
        UserInfoResponseDTO userInfo = infoUserMapper.toUserInfoResponseDTO(suCo.getReporter());

        // Chuyển đổi và trả về dữ liệu đơn lẻ
        return suCoMapper.toTheoDoiDto(suCo, truSoDto, userInfo);
    }
}