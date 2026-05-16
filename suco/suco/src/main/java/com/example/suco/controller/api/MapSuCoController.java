package com.example.suco.controller.api;

import com.example.suco.dto.SuCoMapDto;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.BaoCaoSuCoRepository;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.DieuPhoiSOSService;
import com.example.suco.service.BaoCaoSuCoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/map")
public class MapSuCoController {

    @Autowired
    private BaoCaoSuCoService baoCaoSuCoService;

    @Autowired
    private BaoCaoSuCoRepository repo;

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private DieuPhoiSOSService dieuPhoiService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private com.example.suco.mapper.SuCoMapDtoMapper suCoMapDtoMapper;

    // 1. Lấy danh sách sự cố
    @GetMapping("/su-co")
public List<Object> getSuCoForMap(@RequestParam(value = "idTruSo", required = false) Long idTruSo) {

    List<Object> result = new ArrayList<>();

    // 1. Sự cố thường
    if (idTruSo == null || idTruSo == 0) {
        result.addAll(repo.findAllForMap());
    } else {
        result.addAll(repo.findActiveByTruSo(idTruSo));
    }

    // 2. SOS (SỬA Ở ĐÂY)
    List<TinHieuSOS> sosList;

    if (idTruSo == null || idTruSo == 0) {
        sosList = tinHieuSOSRepository.findAll().stream()
            .filter(s -> !"HOAN_THANH".equals(s.getTrangThai()) && !"HUY_BO".equals(s.getTrangThai()))
            .collect(Collectors.toList());
    } else {
        sosList = tinHieuSOSRepository.findAll().stream()
            .filter(s -> !"HOAN_THANH".equals(s.getTrangThai()) && !"HUY_BO".equals(s.getTrangThai()))
            .filter(s -> idTruSo.equals(s.getIdTruSoDeXuat()) || idTruSo.equals(s.getIdTruSoTiepNhan()))
            .collect(Collectors.toList());
    }

    result.addAll(sosList);

    return result;
}


    // 2. CẬP NHẬT SOS - Đã sửa Topic đồng nhất
    @PostMapping("/sos/cap-nhat-trang-thai/{id}")
    public ResponseEntity<?> updateSosStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) Long idTruSo) {
        return tinHieuSOSRepository.findById(id).map(sos -> {
            sos.setTrangThai(status);
            
            if ("DANG_XU_LY".equals(status)) {
                Long idTruSoTiepNhan = idTruSo != null ? idTruSo : sos.getIdTruSoDeXuat();
                sos.setIdTruSoTiepNhan(idTruSoTiepNhan);
                dieuPhoiService.danhDauDaTiepNhan(id, idTruSoTiepNhan);
            }
            
            if ("HOAN_THANH".equals(status) || "HUY_BO".equals(status)) {
                dieuPhoiService.huyDieuPhoi(id);
            }
            
            TinHieuSOS savedSos = tinHieuSOSRepository.save(sos);

            // Gửi cho User
           if (sos.getUser() != null && sos.getUser().getUid() != null) {
    String userUid = sos.getUser().getUid();
    
    // Đồng nhất gửi về UID chuỗi để Android nhận được
    messagingTemplate.convertAndSend("/topic/user/" + userUid + "/history", "REFRESH");
    
    // Gửi đối tượng đã lưu về App
    messagingTemplate.convertAndSend("/topic/user/" + userUid + "/sos-status", savedSos);
}

            // Gửi cho Web Trụ sở (Đồng nhất prefix /tru-so/ có gạch ngang)
            if (savedSos.getIdTruSoTiepNhan() != null) {
                messagingTemplate.convertAndSend("/topic/tru-so/" + savedSos.getIdTruSoTiepNhan(), savedSos);
            }

            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công", "status", status));
        }).orElse(ResponseEntity.notFound().build());
    }


}