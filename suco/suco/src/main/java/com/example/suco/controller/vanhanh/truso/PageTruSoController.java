package com.example.suco.controller.vanhanh.truso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.suco.config.AppConfig;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/truso")
public class PageTruSoController {

      @Autowired
    private AppConfig appConfig;


    
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

    @GetMapping("/da-tiep-nhan")
    public String sosDaTiepNhan(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/da-tiep-nhan"; // Tab 1
    }

    @GetMapping("/cho-xu-ly")
    public String quanLyCuuTro(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/cho-xu-ly"; // Tab 2
    }

    @GetMapping("/dang-xu-ly")
    public String dangCuuTro(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/dang-xu-ly"; // Tab 3
    }

    @GetMapping("/da-xu-ly")
    public String lichSuCuuTro(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/da-xu-ly"; // Tab 4
    }

}