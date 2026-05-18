package com.example.suco.controller.suco.baocao.truso;

import com.example.suco.dto.SuCoMapDto;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.repository.BaoCaoSuCoRepository;
import com.example.suco.service.suco.baocao.system.mapper.SuCoMapper;
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
public List<SuCoMapDto> getSuCoHienTai(
        @RequestParam(required = false) String status,
        HttpSession session) {

    TruSo current = (TruSo) session.getAttribute("currentTruSo");
    if (current == null) return List.of();

    List<BaoCaoSuCo> entities = repo.findAll();

    List<SuCoMapDto> allActive = entities.stream()
            .map(suCoMapper::convertToDto)
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
    public List<BaoCaoSuCo> getSuCoHistory(HttpSession session) {

        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();

        return repo.findHistoryByTruSo(current.getId());
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