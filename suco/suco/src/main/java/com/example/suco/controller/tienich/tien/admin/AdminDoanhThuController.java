package com.example.suco.controller.tienich.tien.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.suco.service.tienich.tien.admin.DoanhThuService;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonDetailDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin/quan-ly-doanh-thu")
public class AdminDoanhThuController {

    @Autowired
    private DoanhThuService doanhThuService;

    @GetMapping
    public String viewDoanhThu(Model model) {

        model.addAttribute("tongDoanhThu",
                doanhThuService.layTongDoanhThu());

        model.addAttribute("listDoanhThu",
                doanhThuService.layDanhSachDoanhThu());

        model.addAttribute("activePage", "quan-ly-doanh-thu");

        return "admin/quan-ly-doanh-thu";
    }

    // ===== MODAL DETAIL API =====
    @GetMapping("/hoa-don/{id}")
    @ResponseBody
    public ResponseEntity<HoaDonDetailDTO> getChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(doanhThuService.getChiTietHoaDon(id));
    }
}