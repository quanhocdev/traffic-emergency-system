package com.example.suco.service.sos.tinhieu.user;
import com.example.suco.dto.sos.tinhieu.TheoDoiSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.TheoDoiSOSItemResponseDTO;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TheoDoiTinHieuService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    // ===== LIST =====
    public List<TheoDoiSOSItemResponseDTO> layDanhSachItem(String uid) {

        return tinHieuSOSRepository.findByUserUid(uid)
                .stream()
                .map(sos -> {
                    TheoDoiSOSItemResponseDTO dto = new TheoDoiSOSItemResponseDTO();
                    dto.setId(sos.getId());
                    dto.setHinhAnh(sos.getHinhAnh());
                    dto.setTrangThai(sos.getTrangThai());
                    dto.setCreatedAt(sos.getCreatedAt());
                    return dto;
                })
                .toList();
    }

    // ===== DETAIL =====
    public TheoDoiSOSDetailResponseDTO layChiTiet(Long id) {

        var sos = tinHieuSOSRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));

        String tenTruSo = "demo"; // sau này join bảng trụ sở

        return tinHieuMapper.toTheoDoiDto(sos, tenTruSo);
    }
}