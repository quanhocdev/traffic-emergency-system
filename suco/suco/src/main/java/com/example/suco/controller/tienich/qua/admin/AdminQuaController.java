package com.example.suco.controller.tienich.qua.admin;

import com.example.suco.model.Qua;
import com.example.suco.service.tienich.qua.admin.QuaService;
import com.example.suco.dto.tienich.qua.quanly.QuaRequestDTO;
import com.example.suco.dto.tienich.qua.quanly.QuaResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;


@Controller
@RequestMapping("/admin/quan-ly-qua")
public class AdminQuaController {
    @Autowired
    private QuaService quaService;


    @GetMapping
    public String listQua(Model model) {
        model.addAttribute("listQua", quaService.getAllQua());
        model.addAttribute("quaRequestDTO", new QuaRequestDTO());
        model.addAttribute("loaiQua", Qua.LoaiQua.values());
        model.addAttribute("activePage", "quan-ly-qua");

        
        return "admin/quan-ly-qua";
    }

@GetMapping("/all")
@ResponseBody
public List<QuaResponseDTO> getAllQuaApi() {
    return quaService.getAllQua();
}

    @PostMapping("/add")
@ResponseBody
public ResponseEntity<?> addQua(@ModelAttribute QuaRequestDTO quaRequestDTO) {
    try {
        quaService.createQua(quaRequestDTO);

        return ResponseEntity.ok(Map.of(
                "code", "SUCCESS",
                "message", "Thêm quà thành công"
        ));

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", "ERROR",
                "message", e.getMessage()
        ));
    }
}

@DeleteMapping("/delete/{id}")
public ResponseEntity<?> deleteQua(@PathVariable Long id) {
    try {
        quaService.deleteQua(id);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Xóa thành công"
        ));
    } catch (Exception e) {
        return ResponseEntity.status(404).body(Map.of(
            "status", "error",
            "message", "Không tìm thấy quà"
        ));
    }
}

@PatchMapping("/edit/{id}")
@ResponseBody
public ResponseEntity<?> editQua(
        @PathVariable Long id,
        @ModelAttribute QuaRequestDTO dto 
) {
    System.out.println("DTO ngayKetThuc = " + dto.getNgayKetThuc());
    try {
        quaService.updateQua(id, dto);
        return ResponseEntity.ok(Map.of(
    "code", "SUCCESS",
    "message", "Cập nhật thành công"
));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
    "code", "ERROR",
    "message", e.getMessage()
));
    }
}

}
