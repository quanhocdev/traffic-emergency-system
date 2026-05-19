        package com.example.suco.controller.api;

import com.example.suco.dto.sos.TinHieuSOSResponseDTO;
import com.example.suco.model.TinHieuSOS;
        import com.example.suco.model.TruSo;
        import com.example.suco.repository.MuaGoiRepository;
        import com.example.suco.repository.TinHieuSOSRepository;
        import com.example.suco.service.DieuPhoiSOSService;

import com.example.suco.service.sos.system.mapper.*;
        import jakarta.servlet.http.HttpSession;

        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;
        import java.util.*;
        import java.util.stream.Collectors;

        @RestController
        @RequestMapping("/api/tin-hieu-sos")
        @CrossOrigin(origins = "*")
        public class TinHieuSOSApiController {

            @Autowired 
            private TinHieuSOSRepository tinHieuSOSRepository;

            @Autowired
            private DieuPhoiSOSService dieuPhoiService;

            @Autowired
        private MuaGoiRepository muaGoiRepository; 

        @Autowired
        private TinHieuMapper tinHieuMapper;



    @GetMapping("/active")
public ResponseEntity<?> getSosActive(
    @RequestParam(required = false) String status,
    HttpSession session
) {
    TruSo current = (TruSo) session.getAttribute("currentTruSo");
    if (current == null) {
        return ResponseEntity.status(401).body("Chưa đăng nhập");
    }

    // 1. Lấy entity
    List<TinHieuSOS> rawList =
        tinHieuSOSRepository.findActiveByTruSo(current.getId());

    // 2. Filter status (nếu có)
    if (status != null && !status.isEmpty()) {
        rawList = rawList.stream()
                .filter(sos -> status.equalsIgnoreCase(sos.getTrangThai()))
                .collect(Collectors.toList());
    }

    // 3. Map sang DTO + set VIP luôn tại đây
    List<TinHieuSOSResponseDTO> result = rawList.stream()
        .map(sos -> {
            TinHieuSOSResponseDTO dto = tinHieuMapper.mapToDTO(sos);

            boolean laVip = muaGoiRepository.findByUserId(sos.getUserId())
                .stream()
                .anyMatch(mg -> "ACTIVE".equalsIgnoreCase(mg.getTrangThai()));

            if (dto.getUser() != null) {
                dto.getUser().setVip(laVip); // nếu bạn muốn thêm field VIP
            }

            return dto;
        })
        .collect(Collectors.toList());

    return ResponseEntity.ok(result);
    }
       


            @GetMapping("/dieu-phoi/{idSos}")
            public ResponseEntity<?> layThongTinDieuPhoi(@PathVariable Long idSos) {
                return dieuPhoiService.layThongTinDieuPhoi(idSos)
                    .map(dieuPhoi -> {
                        Map<String, Object> ketQua = new HashMap<>();
                        ketQua.put("idSos", dieuPhoi.getIdSos());
                        ketQua.put("trangThaiDieuPhoi", dieuPhoi.getTrangThaiDieuPhoi());
                        ketQua.put("chiMucTruSoHienTai", dieuPhoi.getChiMucTruSoHienTai());
                        ketQua.put("danhSachIdTruSo", dieuPhoi.getDanhSachIdTruSo());
                        ketQua.put("danhSachKhoangCach", dieuPhoi.getDanhSachKhoangCach());
                        ketQua.put("thoiGianGuiTinCuoi", dieuPhoi.getThoiGianGuiTinCuoi());
                        
                        // Tính thời gian còn lại (giây)
                        long giayDaQua = java.time.Duration.between(
                            dieuPhoi.getThoiGianGuiTinCuoi(), 
                            java.time.LocalDateTime.now()
                        ).getSeconds();
                        long thoiGianConLai = Math.max(0, 60 - giayDaQua);
                        ketQua.put("thoiGianConLai", thoiGianConLai);
                        
                        return ResponseEntity.ok(ketQua);
                    })
                    .orElse(ResponseEntity.notFound().build());
            }

        }

