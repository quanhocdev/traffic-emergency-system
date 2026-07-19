package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.suco.config.AppConfig;
import com.example.suco.model.TruSo;
import com.example.suco.repository.vanhanh.TruSoRepository;

@Controller
@RequestMapping("/truso")
public class PageTruSoController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private TruSoRepository truSoRepository; // Thêm repo vào đây

    @GetMapping("/login")
    public String trangLogin() {
        return "truso/login";
    }

    @GetMapping("/trang-chu") 
    public String trangChu(@AuthenticationPrincipal Jwt jwt, Model model) {
        // 1. Lấy ID từ token (Đảm bảo lúc sinh token bạn set Subject là ID)
        Long truSoId = Long.parseLong(jwt.getSubject());
        
        // 2. Tìm object TruSo đầy đủ trong DB
        TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
        
        // 3. Đẩy vào model y hệt ngày xưa để nuôi giao diện Thymeleaf
        model.addAttribute("mapboxToken", appConfig.getMapboxToken());
        model.addAttribute("truSo", truSo); 
        
        return "truso/trang-chu";
    }

    @GetMapping("/da-tiep-nhan")
    public String daTiepNhan(@AuthenticationPrincipal Jwt jwt, Model model) {
        Long truSoId = Long.parseLong(jwt.getSubject());
        TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
        model.addAttribute("truSo", truSo);
        return "truso/da-tiep-nhan"; 
    }

    @GetMapping("/dang-di-chuyen")
    public String dangDiChuyen(@AuthenticationPrincipal Jwt jwt, Model model) {
        Long truSoId = Long.parseLong(jwt.getSubject());
        TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
        model.addAttribute("truSo", truSo);
        return "truso/dang-di-chuyen"; 
    }

    @GetMapping("/dang-xu-ly")
    public String dangXuLy(@AuthenticationPrincipal Jwt jwt, Model model) {
        Long truSoId = Long.parseLong(jwt.getSubject());
        TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
        model.addAttribute("truSo", truSo);
        return "truso/dang-xu-ly"; 
    }

    @GetMapping("/da-xu-ly")
    public String lichSuXuLy(@AuthenticationPrincipal Jwt jwt, Model model) {
        Long truSoId = Long.parseLong(jwt.getSubject());
        TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
        model.addAttribute("truSo", truSo);
        return "truso/da-xu-ly"; 
    }

    @GetMapping("/huy-xu-ly")
    public String lichSuHuyXuLy(@AuthenticationPrincipal Jwt jwt, Model model) {
        Long truSoId = Long.parseLong(jwt.getSubject());
        TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
        model.addAttribute("truSo", truSo);
        return "truso/huy-xu-ly"; 
    }
}