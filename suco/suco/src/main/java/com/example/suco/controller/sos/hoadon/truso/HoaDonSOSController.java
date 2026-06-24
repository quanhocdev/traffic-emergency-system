package com.example.suco.controller.sos.hoadon.truso;

import com.example.suco.dto.sos.hoadon.quanly.HoaDonDetailDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonRequestDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonTruSoResponseDTO;
import com.example.suco.mapper.hoadon.HoaDonCuuHoMapper;
import com.example.suco.model.HoaDon;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.hoadon.HoaDonCuuHoRepository;
import com.example.suco.service.sos.hoadon.truso.HoaDonCuuHoService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/truso/hoa-don")
@CrossOrigin(origins = "*")
public class HoaDonSOSController {

    @Autowired 
    private HoaDonCuuHoService hoaDonService;

    @Autowired
    private HoaDonCuuHoRepository hoaDonRepository;
    
    @Autowired
    private HoaDonCuuHoMapper hoaDonMapper; 

    @Autowired 
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/tao")
    public ResponseEntity<?> tao(
            @RequestBody HoaDonRequestDTO request,
            HttpSession session
    ) {
        try {
            TruSo current = (TruSo) session.getAttribute("currentTruSo");

            if (current == null) {
                return ResponseEntity.status(401)
                        .body("Lỗi: Phiên đăng nhập hết hạn hoặc chưa đăng nhập.");
            }

            HoaDonTruSoResponseDTO response = hoaDonService.taoHoaDon(request, current.getId());

            messagingTemplate.convertAndSend(
                    "/topic/truso/" + current.getId(),
                    response
            );

             if (response.getUser() != null && response.getUser().getEmail() != null) {
                messagingTemplate.convertAndSend(
                        "/topic/user/" + response.getUser().getEmail() + "/invoice",
                        response
                );
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping("/danh-sach")
    public ResponseEntity<?> getDanhSachHoaDonCuaTruSo(HttpSession session) {
        try {
            // 1. Lấy thông tin Trụ sở từ Session
            TruSo current = (TruSo) session.getAttribute("currentTruSo");
            if (current == null) {
                return ResponseEntity.status(401).body("Lỗi: Bạn chưa đăng nhập trụ sở.");
            }

            List<HoaDon> danhSachEntity = hoaDonRepository.findByTrusoIdOrderByIdDesc(current.getId());

            List<HoaDonTruSoResponseDTO> danhSachDto = new ArrayList<>();
            for (HoaDon hd : danhSachEntity) {
                danhSachDto.add(hoaDonMapper.toTruSoDTO(hd));
            }

            return ResponseEntity.ok(danhSachDto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
    @GetMapping("/{id}/chi-tiet-tong-hop")
public ResponseEntity<?> getChiTietHoaDonTongHop(@PathVariable Long id) {
    HoaDonDetailDTO detailDTO = hoaDonService.layChiTietTongHop(id);
    return ResponseEntity.ok(detailDTO);
}
}