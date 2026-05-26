package com.example.suco.controller.suco.baocao.truso.pages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.suco.config.AppConfig;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;

import org.springframework.ui.Model;

import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/truso")
public class PageSuCoController {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
    
    @Autowired
    private AppConfig appConfig;
    
      @GetMapping("/login")
public String trangLogin() {
    return "truso/login";
}

    @GetMapping("/trang-chu") 
    public String trangChu(HttpSession session, Model model) {

    if (session.getAttribute("currentTruSo") == null) {
        return "redirect:/truso/login";
    }

    model.addAttribute("mapboxToken", appConfig.getMapboxToken());

    return "truso/trang-chu";
}

    @GetMapping("/quan-ly-cuu-tro")
    public String quanLyCuuTro(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) {
            return "redirect:/truso/login";
        }
        return "truso/quan-ly-cuu-tro";
    }

    @GetMapping("/dang-cuu-tro")
    public String dangCuuTro(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) {
            return "redirect:/truso/login";
        }
        return "truso/dang-cuu-tro";
    }

    @GetMapping("/lich-su-cuu-tro")
    public String lichSuCuuTro(HttpSession session, Model model) {
        if (session.getAttribute("currentTruSo") == null) {
            return "redirect:/truso/login";
        }
        Object cs = session.getAttribute("currentTruSo");
        java.util.List<TinHieuSOS> lichSu = java.util.Collections.emptyList();
        try {
            if (cs instanceof com.example.suco.model.TruSo) {
                com.example.suco.model.TruSo current = (com.example.suco.model.TruSo) cs;
                // Lấy các SOS đã hoàn thành cho trụ sở này
                lichSu = tinHieuSOSRepository.findByIdTruSoTiepNhanAndTrangThai(current.getId(), "HOAN_THANH");
            }
        } catch (Exception e) {
            // avoid throwing to template; log to stdout for troubleshooting
            System.err.println("Error loading lich su cuu tro: " + e.getMessage());
            e.printStackTrace();
            lichSu = java.util.Collections.emptyList();
        }
        model.addAttribute("lichSuList", lichSu);
        return "truso/lich-su-cuu-tro";
    }


    @GetMapping("/api/sos-cua-toi")
@ResponseBody
public List<TinHieuSOS> sosCuaToi(HttpSession session) {
    TruSo current = (TruSo) session.getAttribute("currentTruSo");
    if (current == null) return List.of();

    return tinHieuSOSRepository.findActiveByTruSo(current.getId());
}
}
