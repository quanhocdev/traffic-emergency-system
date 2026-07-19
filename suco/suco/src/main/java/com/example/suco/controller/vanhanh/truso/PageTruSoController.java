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

    @GetMapping("/trang-chu") 
    public String trangChu(@AuthenticationPrincipal Jwt jwt, Model model) {
        // PHÒNG THỦ 1: Kiểm tra nếu bộ lọc làm mất JWT hoặc JWT rỗng
        if (jwt == null || jwt.getSubject() == null) {
            System.out.println("⚠️ [PageTruSoController] Không tìm thấy JWT Token hợp lệ!");
            return "redirect:/truso/login?error=session_expired";
        }
        
        try {
            Long truSoId = Long.parseLong(jwt.getSubject());
            TruSo truSo = truSoRepository.findById(truSoId).orElse(null);
            
            // PHÒNG THỦ 2: Nếu Token hợp lệ nhưng DB không có bản ghi tương ứng
            if (truSo == null) {
                System.out.println("⚠️ [PageTruSoController] Không tìm thấy Trụ sở với ID: " + truSoId);
                return "redirect:/truso/login?error=not_found";
            }
            
            model.addAttribute("mapboxToken", appConfig.getMapboxToken());
            model.addAttribute("truSo", truSo); // Đảm bảo chắc chắn KHÔNG null khi ra Thymeleaf
            return "truso/trang-chu";

        } catch (NumberFormatException e) {
            System.out.println("⚠️ [PageTruSoController] Subject của JWT sai định dạng số: " + jwt.getSubject());
            return "redirect:/truso/login?error=invalid_token";
        }
    }

    // Áp dụng tương tự cho các trang còn lại để chống sập hoàn toàn
    @GetMapping("/da-tiep-nhan")
    public String daTiepNhan(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null) return "redirect:/truso/login";
        TruSo truSo = truSoRepository.findById(Long.parseLong(jwt.getSubject())).orElse(null);
        if (truSo == null) return "redirect:/truso/login";
        
        model.addAttribute("truSo", truSo);
        return "truso/da-tiep-nhan"; 
    }

    @GetMapping("/dang-di-chuyen")
    public String dangDiChuyen(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null) return "redirect:/truso/login";
        TruSo truSo = truSoRepository.findById(Long.parseLong(jwt.getSubject())).orElse(null);
        if (truSo == null) return "redirect:/truso/login";
        
        model.addAttribute("truSo", truSo);
        return "truso/dang-di-chuyen"; 
    }

    @GetMapping("/dang-xu-ly")
    public String dangXuLy(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null) return "redirect:/truso/login";
        TruSo truSo = truSoRepository.findById(Long.parseLong(jwt.getSubject())).orElse(null);
        if (truSo == null) return "redirect:/truso/login";
        
        model.addAttribute("truSo", truSo);
        return "truso/dang-xu-ly"; 
    }
}