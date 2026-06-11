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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/truso")
public class PageSOSController {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    // ==========================================
    // 🌐 VIEW - GIAO DIỆN CÁC TRANG SOS
    // ==========================================

    @GetMapping("/sos-da-tiep-nhan")
    public String sosDaTiepNhan(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) return "redirect:/truso/login";
        return "truso/sos-da-tiep-nhan"; // Tab 1
    }

    @GetMapping("/quan-ly-cuu-tro") // Chính là trang Chờ Cứu Trợ cũ của bạn
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
    public List<TinHieuSOS> getSosDaTiepNhan(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return tinHieuSOSRepository.findNewAssignedByTruSo(current.getId());
    }

    @GetMapping("/api/sos/cho-cuu-tro")
    @ResponseBody
    public List<TinHieuSOS> getSosChoCuuTro(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        // Lấy danh sách đang chờ cứu trợ (bạn có thể map chuỗi tương ứng trong DB)
        return tinHieuSOSRepository.findByTruSoAndStatus(current.getId(), "CHO_XU_LY");
    }

    @GetMapping("/api/sos-cua-toi") // API này map DTO phức tạp cho trang Đang cứu trợ
    @ResponseBody
    public List<TruSoSOSDetailResponseDTO> sosCuaToi(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();

        List<TinHieuSOS> entities = tinHieuSOSRepository.findActiveByTruSo(current.getId());

        return entities.stream().map(entity -> {
            TruSoSOSDetailResponseDTO dto = new TruSoSOSDetailResponseDTO();
            dto.setId(entity.getId());
            dto.setViDo(entity.getViDo());
            dto.setKinhDo(entity.getKinhDo());
            dto.setDiaChi(entity.getDiaChi());
            dto.setGhiChu(entity.getGhiChu());
            dto.setHinhAnhUrl(entity.getHinhAnh());
            dto.setGhiAmUrl(entity.getGhiAm());
            dto.setThoiGianTao(entity.getCreatedAt());
            dto.setTrangThai(entity.getTrangThai());

            if (entity.getUser() != null) {
                UserMiniDTO userDto = new UserMiniDTO();
                userDto.setId(entity.getUser().getUid());
                userDto.setName(entity.getUser().getName());
                userDto.setEmail(entity.getUser().getEmail());
                userDto.setVip(entity.getIsVip());
                dto.setNguoiGui(userDto);
            }
            if (entity.getHoaDon() != null) {
                HoaDonResponseDTO hdDto = new HoaDonResponseDTO();
                hdDto.setId(entity.getHoaDon().getId());
                hdDto.setSosId(entity.getHoaDon().getSosId());
                hdDto.setTrusoId(entity.getHoaDon().getTrusoId());
                hdDto.setUserId(entity.getHoaDon().getUserId());
                hdDto.setNoiDungXuLy(entity.getHoaDon().getNoiDungXuLy());
                hdDto.setThanhTien(entity.getHoaDon().getThanhTien());
                hdDto.setCreatedAt(entity.getHoaDon().getCreatedAt());
                hdDto.setTrangThai(entity.getHoaDon().getTrangThai());
                dto.setHoaDon(hdDto);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // ==========================================
    // 🎮 API - CHUYỂN TRẠNG THÁI SOS
    // ==========================================

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