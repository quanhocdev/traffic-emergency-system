package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;

import com.example.suco.config.AppConfig;
import com.example.suco.model.TruSo;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.repository.suco.baocao.SuCoTruSoRepository;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/truso")
public class PageSuCoController {

    @Autowired
    private SuCoTruSoRepository suCoTruSoRepository;
    
    @Autowired
    private AppConfig appConfig;

    // ==========================================
    // 🌐 VIEW - GIAO DIỆN CÁC TRANG SỰ CỐ
    // ==========================================

    @GetMapping("/login")
    public String trangLogin() {
        return "truso/login";
    }

    @GetMapping("/trang-chu") 
    public String trangChu(HttpSession session, Model model) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        model.addAttribute("mapboxToken", appConfig.getMapboxToken());
        return "truso/trang-chu";
    }

    @GetMapping("/su-co-da-tiep-nhan")
    public String suCoDaTiepNhan(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/su-co-da-tiep-nhan"; // Tab 1
    }

    @GetMapping("/su-co-cho-xu-ly")
    public String suCoChoXuLy(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/su-co-cho-xu-ly"; // Tab 2
    }

    @GetMapping("/su-co-dang-xu-ly")
    public String suCoDangXuLy(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/su-co-dang-xu-ly"; // Tab 3
    }

    @GetMapping("/lich-su-su-co")
    public String lichSuSuCo(HttpSession session, Model model) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        
        // Đẩy thẳng danh sách lịch sử vào Model để render SSR bằng Thymeleaf nếu muốn
        List<BaoCaoSuCo> lichSu = suCoTruSoRepository.findHistoryByTruSo(current.getId());
        model.addAttribute("lichSuSuCoList", lichSu);
        return "truso/lich-su-su-co"; // Tab 4
    }

    // ==========================================
    // ⚡ API - LẤY DỮ LIỆU SỰ CỐ (CHO JS CALL)
    // ==========================================

    @GetMapping("/api/su-co/da-tiep-nhan")
    @ResponseBody
    public List<BaoCaoSuCo> getSuCoDaTiepNhan(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findNewAssignedByTruSo(current.getId());
    }

    @GetMapping("/api/su-co/cho-xu-ly")
    @ResponseBody
    public List<BaoCaoSuCo> getSuCoChoXuLy(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findPendingByTruSo(current.getId());
    }

    @GetMapping("/api/su-co/dang-xu-ly")
    @ResponseBody
    public List<BaoCaoSuCo> getSuCoDangXuLy(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findActiveByTruSo(current.getId());
    }

    // ==========================================
    // 🎮 API - CHUYỂN TRẠNG THÁI SỰ CỐ
    // ==========================================

    @PostMapping("/api/su-co/{id}/di-chuyen-ngay")
    @ResponseBody
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