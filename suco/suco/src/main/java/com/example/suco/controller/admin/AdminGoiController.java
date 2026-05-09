package com.example.suco.controller.admin;

import com.example.suco.dto.GoiDto;
import com.example.suco.service.GoiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/goi")
public class AdminGoiController {

    @Autowired
    private GoiService goiService;

    @GetMapping
    public String hienThiQuanLyGoi(Model model) {
        model.addAttribute("listGoi", goiService.getAllGoi());
        model.addAttribute("goiMoi", new GoiDto()); // Dùng cho form thêm mới
        return "admin/quan-ly-goi";
    }

    // @PostMapping("/save")
    // public String luuGoi(@ModelAttribute("goiMoi") GoiDto goiDto) {
    //     goiService.saveGoi(goiDto);
    //     return "redirect:/admin/goi";
    // }

    // @GetMapping("/xoa/{id}")
    // public String xoaGoi(@PathVariable Long id) {
    //     goiService.deleteGoi(id);
    //     return "redirect:/admin/goi";
    // }
}