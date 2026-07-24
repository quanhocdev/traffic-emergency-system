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
    private TruSoRepository truSoRepository;

    @GetMapping("/login")
    public String trangLogin() {
        return "truso/login";
    }

    // 1. TRANG CHỦ (BẢN ĐỒ)
    @GetMapping("/trang-chu") 
    public String trangChu(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null || jwt.getSubject() == null) {
            return "redirect:/truso/login?error=session_expired";
        }
        try {
            Long truSoId = Long.parseLong(jwt.getSubject());
            TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
            if (truSo == null) {
                return "redirect:/truso/login?error=not_found";
            }
            model.addAttribute("mapboxToken", appConfig.getMapboxToken());
            model.addAttribute("truSo", truSo); 
            return "truso/trang-chu";
        } catch (NumberFormatException e) {
            return "redirect:/truso/login?error=invalid_token";
        }
    }

    // 2. ĐÃ TIẾP NHẬN
    @GetMapping("/da-tiep-nhan")
    public String daTiepNhan(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null || jwt.getSubject() == null) return "redirect:/truso/login?error=session_expired";
        try {
            Long truSoId = Long.parseLong(jwt.getSubject());
            TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
            if (truSo == null) return "redirect:/truso/login?error=not_found";
            
            model.addAttribute("truSo", truSo);
            return "truso/da-tiep-nhan"; 
        } catch (NumberFormatException e) {
            return "redirect:/truso/login?error=invalid_token";
        }
    }

    // 3. ĐANG DI CHUYỂN
    @GetMapping("/dang-di-chuyen")
    public String dangDiChuyen(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null || jwt.getSubject() == null) return "redirect:/truso/login?error=session_expired";
        try {
            Long truSoId = Long.parseLong(jwt.getSubject());
            TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
            if (truSo == null) return "redirect:/truso/login?error=not_found";
            
            model.addAttribute("truSo", truSo);
            return "truso/dang-di-chuyen"; 
        } catch (NumberFormatException e) {
            return "redirect:/truso/login?error=invalid_token";
        }
    }

    // 4. ĐANG CỨU HỘ (ĐANG XỬ LÝ)
    @GetMapping("/dang-xu-ly")
    public String dangXuLy(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null || jwt.getSubject() == null) return "redirect:/truso/login?error=session_expired";
        try {
            Long truSoId = Long.parseLong(jwt.getSubject());
            TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
            if (truSo == null) return "redirect:/truso/login?error=not_found";
            
            model.addAttribute("truSo", truSo);
            return "truso/dang-xu-ly"; 
        } catch (NumberFormatException e) {
            return "redirect:/truso/login?error=invalid_token";
        }
    }

    // 5. LỊCH SỬ CỨU HỘ (ĐÃ XỬ LÝ)
    @GetMapping("/da-xu-ly")
    public String daXuLy(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null || jwt.getSubject() == null) return "redirect:/truso/login?error=session_expired";
        try {
            Long truSoId = Long.parseLong(jwt.getSubject());
            TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
            if (truSo == null) return "redirect:/truso/login?error=not_found";
            
            model.addAttribute("truSo", truSo);
            return "truso/da-xu-ly"; // Trả về file da-xu-ly.html của bạn
        } catch (NumberFormatException e) {
            return "redirect:/truso/login?error=invalid_token";
        }
    }

    // 6. ĐÃ BỔ SUNG: LỊCH SỬ HỦY XỬ LÝ
    @GetMapping("/huy-xu-ly")
    public String huyXuLy(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null || jwt.getSubject() == null) return "redirect:/truso/login?error=session_expired";
        try {
            Long truSoId = Long.parseLong(jwt.getSubject());
            TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
            if (truSo == null) return "redirect:/truso/login?error=not_found";
            
            model.addAttribute("truSo", truSo);
            return "truso/huy-xu-ly"; // Trả về file huy-xu-ly.html tương ứng nếu có
        } catch (NumberFormatException e) {
            return "redirect:/truso/login?error=invalid_token";
        }
    }
}