package com.example.suco.controller.sos.goi.admin;

import com.example.suco.dto.sos.goi.quanly.GoiRequestDTO;
import com.example.suco.dto.sos.goi.quanly.GoiResponseDTO;
import com.example.suco.service.sos.goi.admin.CRUDGoiService;

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
        @RequestBody GoiRequestDTO dto
) {
    GoiResponseDTO result = goiService.createGoi(dto);
    return ResponseEntity.ok(result);
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
        @RequestBody GoiRequestDTO dto
) {
    GoiResponseDTO result = goiService.updateGoi(id, dto);
    return ResponseEntity.ok(result);
}
}