package com.example.suco.controller.admin;

import com.example.suco.model.LoaiSuCo;
import com.example.suco.service.LoaiSuCoService;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/admin/loai-su-co")
public class LoaiSuCoAdminController {

    private final LoaiSuCoService service;

    public LoaiSuCoAdminController(LoaiSuCoService service) {
        this.service = service;
    }

    // MỞ TRANG QUẢN LÝ
    @GetMapping
    public String page(Model model) {
        model.addAttribute("list", service.getLoaiSuCo());
        return "admin/loai-su-co"; // trỏ tới loai-su-co.html
    }

  @PostMapping("/create")
public String create(@RequestParam String ten,
                     @RequestParam(value = "iconFile", required = false) MultipartFile file) throws IOException {
    
    LoaiSuCo l = new LoaiSuCo();
    l.setTen(ten);

    if (file != null && !file.isEmpty()) {
        System.out.println("Nhận được file: " + file.getOriginalFilename()); // Kiểm tra log này
        
        String filename = file.getOriginalFilename();
        // Dùng đường dẫn tuyệt đối để tránh lỗi FileNotFound
        String uploadDir = System.getProperty("user.dir") + "/uploads/icons/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(file.getInputStream(), uploadPath.resolve(filename), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        // Gán giá trị vào object trước khi lưu
        l.setIconUrl("/uploads/icons/" + filename);
        System.out.println("Đã gán iconUrl: " + l.getIconUrl());
    } else {
        System.out.println("File bị null hoặc trống!");
    }

    service.createLoaiSuCo(ten, file);
    return "redirect:/admin/loai-su-co";
}

@PostMapping("/api/create")
@ResponseBody
public ResponseEntity<?> createApi(
        @RequestParam String ten,
        @RequestParam(value = "iconFile", required = false) MultipartFile file
) throws IOException {

    LoaiSuCo saved = service.createLoaiSuCo(ten, file);
    return ResponseEntity.ok(saved);
}
@DeleteMapping("/delete/{id}")
@ResponseBody
public ResponseEntity<?> delete(@PathVariable Long id) {
    service.deleteLoaiSuCo(id);
    return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
}
@PutMapping("/api/update/{id}")
@ResponseBody
public ResponseEntity<?> updateApi(
        @PathVariable Long id,
        @RequestParam String ten,
        @RequestParam(value = "iconFile", required = false) MultipartFile file
) throws IOException {
    LoaiSuCo updated = service.updateLoaiSuCo(id, ten, file);
    return ResponseEntity.ok(updated);
}
}
