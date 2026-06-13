package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonResponseDTO;
import com.example.suco.dto.sos.tinhieu.UserMiniDTO;
import com.example.suco.dto.sos.tinhieu.truso.TruSoSOSDetailResponseDTO;
import com.example.suco.mapper.TinHieuMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/truso")
public class PageSOSController {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
     @Autowired
private TinHieuMapper tinHieuMapper; 


    @GetMapping("/sos-da-tiep-nhan")
    public String sosDaTiepNhan(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/sos-da-tiep-nhan"; // Tab 1
    }

    @GetMapping("/quan-ly-cuu-tro")
    public String quanLyCuuTro(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/quan-ly-cuu-tro"; // Tab 2
    }

    @GetMapping("/dang-cuu-tro")
    public String dangCuuTro(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/dang-cuu-tro"; // Tab 3
    }

    @GetMapping("/lich-su-cuu-tro")
    public String lichSuCuuTro(HttpSession session, Model model) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        
        List<TinHieuSOS> lichSu = tinHieuSOSRepository.findHistoryByTruSo(current.getId());
        model.addAttribute("lichSuList", lichSu); // Tab 4
        return "truso/lich-su-cuu-tro";
    }

    // ==========================================
    // ⚡ API - LẤY DỮ LIỆU SOS (DÙNG REPO MỚI)
    // ==========================================

    @GetMapping("/api/sos/da-tiep-nhan")
@ResponseBody
public List<TruSoSOSDetailResponseDTO> getSosDaTiepNhan(HttpSession session) {
    TruSo current = (TruSo) session.getAttribute("currentTruSo");
    if (current == null) return List.of();
    
    // Đổ qua stream map như các hàm khác
    return tinHieuSOSRepository.findNewAssignedByTruSo(current.getId()).stream()
            .map(tinHieuMapper::toTruSoDetailDto)
            .toList();
}

@GetMapping("/api/sos/cho-xu-ly")
@ResponseBody
public List<TruSoSOSDetailResponseDTO> getSosChoCuuTro(HttpSession session) {
    // 1. Kiểm tra session Trụ sở
    TruSo current = (TruSo) session.getAttribute("currentTruSo");
    if (current == null) return List.of();
    
    // 2. Gọi hàm findMovingByTruSo chuyên biệt quét trạng thái CHO_XU_LY mà ta vừa thêm trong Repository
    List<TinHieuSOS> movingEntities = tinHieuSOSRepository.findMovingByTruSo(current.getId());
    
    // 3. Đẩy qua mapper có sẵn của bạn để map đầy đủ thông tin hóa đơn, người gửi giống các API khác
    return movingEntities.stream()
            .map(tinHieuMapper::toTruSoDetailDto)
            .collect(Collectors.toList());
}
   

@GetMapping("/api/sos/dang-xu-ly")
@ResponseBody
public List<TruSoSOSDetailResponseDTO> sosCuaToi(HttpSession session) {
    // 1. Lấy thông tin Trụ sở đang đăng nhập từ Session
    TruSo current = (TruSo) session.getAttribute("currentTruSo");
    if (current == null) {
        return List.of(); // Trả về mảng rỗng nếu chưa đăng nhập hoặc mất session
    }

    // 2. Gọi Repository lấy danh sách thực thể (Đổi tên thành sosEntities để tránh trùng biến cục bộ)
    List<TinHieuSOS> sosEntities = tinHieuSOSRepository.findActiveByTruSo(current.getId());

    // 3. Đẩy qua TinHieuMapper đã cấu trúc để tự động chuyển sang DTO kèm Hóa Đơn
    return sosEntities.stream()
            .map(tinHieuMapper::toTruSoDetailDto)
            .collect(Collectors.toList());
}
    @PostMapping("/api/sos/{id}/di-chuyen-ngay")
    @ResponseBody
    public ResponseEntity<?> sosDiChuyenNgay(@PathVariable Long id, HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        TinHieuSOS sos = tinHieuSOSRepository.findById(id).orElse(null);
        if (sos == null) return ResponseEntity.notFound().build();

        if (sos.getIdTruSoTiepNhan() == null || !sos.getIdTruSoTiepNhan().equals(current.getId())) {
            return ResponseEntity.badRequest().body("Yêu cầu SOS không thuộc trụ sở này");
        }

        sos.setTrangThai("CHO_XU_LY"); 
        tinHieuSOSRepository.save(sos);
        return ResponseEntity.ok().body(Map.of("success", true));
    }
}