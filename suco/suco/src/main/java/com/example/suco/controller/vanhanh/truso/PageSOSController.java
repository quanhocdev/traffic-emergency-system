package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; 
import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.dto.sos.tinhieu.truso.TruSoSOSDetailResponseDTO;
import com.example.suco.mapper.TinHieuMapper;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/truso")
public class PageSOSController {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
    
    @Autowired
    private TinHieuMapper tinHieuMapper; 

    private Long getTruSoId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }

    @GetMapping("/api/sos/da-tiep-nhan")
    public List<TruSoSOSDetailResponseDTO> getSosDaTiepNhan(@AuthenticationPrincipal Jwt jwt) {
        return tinHieuSOSRepository.findNewAssignedByTruSo(getTruSoId(jwt)).stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .toList();
    }

    @GetMapping("/api/sos/dang-di-chuyen")
    public List<TruSoSOSDetailResponseDTO> getSosDangDiChuyen(@AuthenticationPrincipal Jwt jwt) {
        List<TinHieuSOS> movingEntities = tinHieuSOSRepository.findMovingByTruSo(getTruSoId(jwt));
        return movingEntities.stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/sos/dang-xu-ly")
    public List<TruSoSOSDetailResponseDTO> getSosDangXuLy(@AuthenticationPrincipal Jwt jwt) {
        List<TinHieuSOS> sosEntities = tinHieuSOSRepository.findActiveByTruSo(getTruSoId(jwt));
        return sosEntities.stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/sos/lich-su")
    public List<TruSoSOSDetailResponseDTO> getSosLichSu(@AuthenticationPrincipal Jwt jwt) {
        List<TinHieuSOS> lichSuEntities = tinHieuSOSRepository.findHistoryByTruSo(getTruSoId(jwt));
        return lichSuEntities.stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/sos/huy-xu-ly")
    public List<TruSoSOSDetailResponseDTO> getSosHuyXuLy(@AuthenticationPrincipal Jwt jwt) {
        List<TinHieuSOS> huyXuLyEntities = tinHieuSOSRepository.findCancelByTruSo(getTruSoId(jwt));
        return huyXuLyEntities.stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .collect(Collectors.toList());
    }
}