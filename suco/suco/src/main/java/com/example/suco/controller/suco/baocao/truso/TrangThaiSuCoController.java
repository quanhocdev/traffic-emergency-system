package com.example.suco.controller.suco.baocao.truso;

import com.example.suco.dto.suco.baocao.TruSoSuCoDetailResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.model.TruSo;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.service.suco.baocao.truso.TrangThaiSuCoService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/su-co")
public class TrangThaiSuCoController {

    @Autowired
    private BaoCaoSuCoRepository repo;

@Autowired
private SuCoMapper suCoMapper;

    @Autowired
    private TrangThaiSuCoService trangThaiServiceService;
    

@GetMapping("/danh-sach-hien-tai")
public List<TruSoSuCoDetailResponseDTO> getSuCoHienTai(
        @RequestParam(required = false) String status,
        HttpSession session) {

    TruSo current = (TruSo) session.getAttribute("currentTruSo");

    if (current == null) {
        return List.of();
    }

    List<TruSoSuCoDetailResponseDTO> allActive =
            repo.findAll()
                    .stream()
                    .map(suCoMapper::toTruSoDetailDto)
                    .toList();

    if (status != null && !status.isEmpty()) {
        return allActive.stream()
                .filter(s -> s.getTrangThaiXuLy() != null
                        && s.getTrangThaiXuLy().equalsIgnoreCase(status))
                .toList();
    }

    return allActive;
}
  
    @GetMapping("/lich-su")
public List<TruSoSuCoDetailResponseDTO> getSuCoHistory(
        HttpSession session
) {
    TruSo current = (TruSo) session.getAttribute("currentTruSo");

    if (current == null) {
        return List.of();
    }

    return repo.findHistoryByTruSo(current.getId())
            .stream()
            .map(suCoMapper::toTruSoDetailDto)
            .toList();
}


    @PatchMapping("/cap-nhat-trang-thai/{id}")
    public ResponseEntity<?> updateSuCoStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        String status = body.get("status");
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Vui lòng đăng nhập tài khoản trụ sở!"));
        }
        Map<String, Object> result =
                trangThaiServiceService.updateSuCoStatus(id, status, current);
        return ResponseEntity.ok(result);
    }
}