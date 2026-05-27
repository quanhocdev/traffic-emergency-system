package com.example.suco.controller.sos.goi.admin;

import com.example.suco.dto.sos.goi.GoiDto;
import com.example.suco.model.Goi;
import com.example.suco.service.sos.payment.goi.admin.CRUDGoiService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/goi")
public class CRUDGoiController {

    @Autowired
    private CRUDGoiService goiService;

    @GetMapping
    public String hienThiQuanLyGoi(Model model) {
        model.addAttribute("listGoi", goiService.getAllGoi());
        model.addAttribute("activePage", "quan-ly-goi");
        return "admin/quan-ly-goi";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createGoi(
        @ModelAttribute GoiDto dto
    ) {

    Goi goi = goiService.createGoi(dto);

    return ResponseEntity.ok(goi);
}

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteGoi(@PathVariable Long id) {
        goiService.deleteGoi(id);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }

    @PatchMapping("/{id}")
@ResponseBody
public ResponseEntity<?> updateGoi(
        @PathVariable Long id,
        @ModelAttribute GoiDto dto
) {
    Goi updated = goiService.updateGoi(id, dto);
    return ResponseEntity.ok(updated);
}
}