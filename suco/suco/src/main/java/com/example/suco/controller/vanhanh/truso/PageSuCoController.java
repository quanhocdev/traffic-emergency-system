package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; // 🔥 Dùng RestController
import org.springframework.http.ResponseEntity;

import com.example.suco.model.TruSo;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.repository.suco.baocao.SuCoTruSoRepository;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;

@RestController // 🔥 Thay bằng @RestController để bỏ hết @ResponseBody
@RequestMapping("/truso")
public class PageSuCoController {

    @Autowired
    private SuCoTruSoRepository suCoTruSoRepository;

    @GetMapping("/api/su-co/da-tiep-nhan")
    public List<BaoCaoSuCo> getSuCoDaTiepNhan(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findNewAssignedByTruSo(current.getId());
    }

    @GetMapping("/api/su-co/cho-xu-ly")
    public List<BaoCaoSuCo> getSuCoChoXuLy(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findPendingByTruSo(current.getId());
    }

    @GetMapping("/api/su-co/dang-xu-ly")
    public List<BaoCaoSuCo> getSuCoDangXuLy(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findActiveByTruSo(current.getId());
    }

    @GetMapping("/api/su-co/da-xu-ly")
    public List<BaoCaoSuCo> getSuCoDaXuLy(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findHistoryByTruSo(current.getId());
    }

    @PostMapping("/api/su-co/{id}/di-chuyen-ngay")
    public ResponseEntity<?> diChuyenNgay(@PathVariable Long id, HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        BaoCaoSuCo suCo = suCoTruSoRepository.findById(id).orElse(null);
        if (suCo == null) return ResponseEntity.notFound().build();

        if (suCo.getTruSoTiepNhan() == null || !suCo.getTruSoTiepNhan().getId().equals(current.getId())) {
            return ResponseEntity.badRequest().body("Sự cố không thuộc trụ sở này");
        }

        if (!suCo.getTrangThaiXuLy().canTransitionTo(TrangThaiXuLy.CHO_XU_LY)) {
            return ResponseEntity.badRequest().body("Không thể chuyển sang Chờ xử lý");
        }

        suCo.setTrangThaiXuLy(TrangThaiXuLy.CHO_XU_LY);
        suCoTruSoRepository.save(suCo);
        return ResponseEntity.ok().body(Map.of("success", true));
    }
}