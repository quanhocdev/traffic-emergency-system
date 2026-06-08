package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.suco.config.AppConfig;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;

import org.springframework.ui.Model;
import com.example.suco.dto.sos.tinhieu.TruSoSOSDetailResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonResponseDTO;
import com.example.suco.dto.sos.tinhieu.UserMiniDTO; // Nhớ import cả UserMiniDTO nếu nằm ở package khác
import java.util.stream.Collectors;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/truso")
public class PageSuCoController {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
    
    @Autowired
    private AppConfig appConfig;
    
      @GetMapping("/login")
public String trangLogin() {
    return "truso/login";
}

    @GetMapping("/trang-chu") 
    public String trangChu(HttpSession session, Model model) {

    if (session.getAttribute("currentTruSo") == null) {
        return "redirect:/truso/login";
    }

    model.addAttribute("mapboxToken", appConfig.getMapboxToken());

    return "truso/trang-chu";
}

    @GetMapping("/quan-ly-cuu-tro")
    public String quanLyCuuTro(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) {
            return "redirect:/truso/login";
        }
        return "truso/quan-ly-cuu-tro";
    }

    @GetMapping("/dang-cuu-tro")
    public String dangCuuTro(HttpSession session) {
        if (session.getAttribute("currentTruSo") == null) {
            return "redirect:/truso/login";
        }
        return "truso/dang-cuu-tro";
    }

    @GetMapping("/lich-su-cuu-tro")
    public String lichSuCuuTro(HttpSession session, Model model) {
        if (session.getAttribute("currentTruSo") == null) {
            return "redirect:/truso/login";
        }
        Object cs = session.getAttribute("currentTruSo");
        java.util.List<TinHieuSOS> lichSu = java.util.Collections.emptyList();
        try {
            if (cs instanceof com.example.suco.model.TruSo) {
                com.example.suco.model.TruSo current = (com.example.suco.model.TruSo) cs;
                // Lấy các SOS đã hoàn thành cho trụ sở này
                lichSu = tinHieuSOSRepository.findByIdTruSoTiepNhanAndTrangThai(current.getId(), "HOAN_THANH");
            }
        } catch (Exception e) {
            // avoid throwing to template; log to stdout for troubleshooting
            System.err.println("Error loading lich su cuu tro: " + e.getMessage());
            e.printStackTrace();
            lichSu = java.util.Collections.emptyList();
        }
        model.addAttribute("lichSuList", lichSu);
        return "truso/lich-su-cuu-tro";
    }


   @GetMapping("/api/sos-cua-toi")
    @ResponseBody
    public List<TruSoSOSDetailResponseDTO> sosCuaToi(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();

        List<TinHieuSOS> entities = tinHieuSOSRepository.findActiveByTruSo(current.getId());

        return entities.stream().map(entity -> {
            TruSoSOSDetailResponseDTO dto = new TruSoSOSDetailResponseDTO();
            
            // 1. Map thông tin SOS cơ bản
            dto.setId(entity.getId());
            dto.setViDo(entity.getViDo());
            dto.setKinhDo(entity.getKinhDo());
            dto.setDiaChi(entity.getDiaChi());
            dto.setGhiChu(entity.getGhiChu());
            dto.setHinhAnhUrl(entity.getHinhAnh());
            dto.setGhiAmUrl(entity.getGhiAm());
            dto.setThoiGianTao(entity.getCreatedAt());
            dto.setTrangThai(entity.getTrangThai());

            // 🌟 CÁCH 1: Nếu giao diện JavaScript đọc thuộc tính VIP trực tiếp từ SOS (s.isVip)
            // Bạn nhớ mở file TruSoSOSDetailResponseDTO.java thêm trường 'private boolean isVip;' cùng getter/setter nhé!
            // dto.setIsVip(entity.getIsVip()); 

            // 2. Map thông tin người gửi (UserMiniDTO) + CHECK VIP Ở ĐÂY
            if (entity.getUser() != null) {
                UserMiniDTO userDto = new UserMiniDTO();
                
                userDto.setId(entity.getUser().getUid());
                userDto.setName(entity.getUser().getName());
                userDto.setEmail(entity.getUser().getEmail());
                
                // 🌟 CÁCH 2: Gán VIP vào đối tượng Người gửi (s.nguoiGui.vip)
                // (Chọn cách này nếu thực thể User gốc của bạn có hàm check VIP, ví dụ: entity.getUser().getIsVip())
                // userDto.setVip(entity.getUser().getIsVip()); 
                
                // Hoặc nếu bạn muốn lấy luôn trạng thái VIP tạm thời từ thực thể SOS gán qua cho User:
                userDto.setVip(entity.getIsVip());

                dto.setNguoiGui(userDto);
            }

            // 3. Map thông tin Hóa đơn
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
            } else {
                dto.setHoaDon(null);
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
