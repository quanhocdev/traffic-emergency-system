package com.example.suco.service.sos.tinhieu.user;
import com.example.suco.dto.sos.tinhieu.TheoDoiSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.TheoDoiSOSItemResponseDTO;
import com.example.suco.dto.vanhanh.truso.TruSoMapDto;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.xacthuc.truso.TruSoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TheoDoiTinHieuService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    @Autowired
private TruSoService truSoService;

    // ===== LIST =====
   public List<TheoDoiSOSItemResponseDTO> layDanhSachItem(
        String uid
) {

    return tinHieuSOSRepository.findByUserUid(uid)
            .stream()
            .map(sos -> {

                String tenTruSo = null;

                if (sos.getIdTruSoTiepNhan() != null) {

                    var truSo =
                            truSoService.timTruSoTheoId(
                                    sos.getIdTruSoTiepNhan()
                            );

                    if (truSo != null) {
                        tenTruSo = truSo.getTenTruSo();
                    }
                }

                return tinHieuMapper.toTheoDoiItemDto(
                        sos,
                        tenTruSo
                );
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