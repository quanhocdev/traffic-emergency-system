package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; // 🔥 Dùng RestController
import org.springframework.http.ResponseEntity;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.dto.sos.tinhieu.truso.TruSoSOSDetailResponseDTO;
import com.example.suco.mapper.TinHieuMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/truso")
public class PageSOSController {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
    
    @Autowired
    private TinHieuMapper tinHieuMapper; 

    @GetMapping("/api/sos/da-tiep-nhan")
    public List<TruSoSOSDetailResponseDTO> getSosDaTiepNhan(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        
        return tinHieuSOSRepository.findNewAssignedByTruSo(current.getId()).stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .toList();
    }

    @GetMapping("/api/sos/cho-xu-ly")
    public List<TruSoSOSDetailResponseDTO> getSosChoCuuTro(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        
        List<TinHieuSOS> movingEntities = tinHieuSOSRepository.findMovingByTruSo(current.getId());
        
        return movingEntities.stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/sos/dang-xu-ly")
    public List<TruSoSOSDetailResponseDTO> sosCuaToi(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();

        List<TinHieuSOS> sosEntities = tinHieuSOSRepository.findActiveByTruSo(current.getId());

        return sosEntities.stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/sos/lich-su")
    public List<TruSoSOSDetailResponseDTO> getSosLichSu(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        
        List<TinHieuSOS> lichSuEntities = tinHieuSOSRepository.findHistoryByTruSo(current.getId());
        
        return lichSuEntities.stream()
                .map(tinHieuMapper::toTruSoDetailDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/api/sos/{id}/di-chuyen-ngay")
    public ResponseEntity<?> sosDiChuyenNgay(@PathVariable Long id, HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }
        TinHieuSOS sos = tinHieuSOSRepository.findById(id).orElse(null);
        if (sos == null) return ResponseEntity.notFound().build();

        if (sos.getIdTruSoTiepNhan() == null || !sos.getIdTruSoTiepNhan().equals(current.getId())) {
            return ResponseEntity.badRequest().body("Yêu cầu SOS không thuộc trụ sở này");
        }

        sos.setTrangThai("CHO_XU_LY"); 
        tinHieuSOSRepository.save(sos);
        return ResponseEntity.ok().body(Map.of("success", true));
    }
}