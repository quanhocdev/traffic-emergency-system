package com.example.suco.controller.sos.goi.admin;

import com.example.suco.dto.sos.goi.GoiDto;
import com.example.suco.model.Goi;
import com.example.suco.service.GoiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/goi")
public class CRUDGoiController {

    @Autowired
    private GoiService goiService;

    @GetMapping
    public String hienThiQuanLyGoi(Model model) {
        model.addAttribute("listGoi", goiService.getAllGoi());
        model.addAttribute("goiMoi", new GoiDto()); 
        model.addAttribute("activePage", "quan-ly-goi");
        return "admin/quan-ly-goi";
    }


    @GetMapping("/danh-sach")
    public ResponseEntity<?> getDanhSachGoi() {
        return ResponseEntity.ok(goiService.getAllGoi());
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createGoi(@RequestBody GoiDto dto) {
        Goi goi = goiService.createGoi(dto);
        return ResponseEntity.ok(goi);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteGoi(@PathVariable Long id) {
        goiService.deleteGoi(id);
        return ResponseEntity.ok("Xóa thành công");
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateGoi(@PathVariable Long id,
                                   @RequestBody GoiDto dto) {
    goiService.updateGoi(id, dto);
    return ResponseEntity.ok("Cập nhật thành công");
    }
}