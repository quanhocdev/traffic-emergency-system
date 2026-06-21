package com.example.suco.controller.sos.tinhieu.admin;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/admin/quan-ly-sos")
public class CRUDSosController {
    @GetMapping
    public String hienThiQuanLySos(Model model) {
        model.addAttribute("activePage", "quan-ly-sos");
        return "admin/quan-ly-sos";
    }
}
