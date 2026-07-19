package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; 
import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.dto.sos.tinhieu.truso.TruSoSOSDetailResponseDTO;
import com.example.suco.mapper.TinHieuMapper;
import java.util.List;

@RestController
@RequestMapping("/truso")
public class PageSOSController {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
    
    @Autowired
    private TinHieuMapper tinHieuMapper; 

    // PHÒNG THỦ: Kiểm tra bảo mật token tránh lỗi 500 trắng trang
    private Long getTruSoId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn");
        }
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Mã trụ sở sai định dạng số");
        }
    }

    @GetMapping("/api/sos/da-tiep-nhan")
    public List<TruSoSOSDetailResponseDTO> getSosDaTiepNhan(@AuthenticationPrincipal Jwt jwt) {
        return tinHieuSOSRepository.findNewAssignedByTruSo(getTruSoId(jwt)).stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .toList();
    }

    @GetMapping("/api/sos/dang-di-chuyen")
    public List<TruSoSOSDetailResponseDTO> getSosDangDiChuyen(@AuthenticationPrincipal Jwt jwt) {
        return tinHieuSOSRepository.findMovingByTruSo(getTruSoId(jwt)).stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .toList(); // Rút gọn gọn gàng hơn
    }

    @GetMapping("/api/sos/dang-xu-ly")
    public List<TruSoSOSDetailResponseDTO> getSosDangXuLy(@AuthenticationPrincipal Jwt jwt) {
        return tinHieuSOSRepository.findActiveByTruSo(getTruSoId(jwt)).stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .toList();
    }

    // ĐÃ SỬA: Đổi từ /api/sos/lich-su thành /api/sos/da-xu-ly để khớp cấu trúc bên Sự cố
    @GetMapping("/api/sos/da-xu-ly")
    public List<TruSoSOSDetailResponseDTO> getSosDaXuLy(@AuthenticationPrincipal Jwt jwt) {
        return tinHieuSOSRepository.findHistoryByTruSo(getTruSoId(jwt)).stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .toList();
    }

    @GetMapping("/api/sos/huy-xu-ly")
    public List<TruSoSOSDetailResponseDTO> getSosHuyXuLy(@AuthenticationPrincipal Jwt jwt) {
        return tinHieuSOSRepository.findCancelByTruSo(getTruSoId(jwt)).stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .toList();
    }
}