package com.example.suco.controller.sos.payment.hoadon.truso;

import com.example.suco.model.HoaDon;
import com.example.suco.model.TruSo;
import com.example.suco.repository.payment.HoaDonRepository;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.sos.payment.hoadon.truso.HoaDonSOSService;
import com.example.suco.dto.sos.payment.hoadon.request.HoaDonRequestDTO;
import com.example.suco.dto.sos.payment.hoadon.response.HoaDonResponseDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;

@RestController
@RequestMapping("/truso/hoa-don")
@CrossOrigin(origins = "*")
public class HoaDonSOSController {

    @Autowired 
    private HoaDonSOSService hoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired 
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/tao")
public ResponseEntity<?> tao(
        @RequestBody HoaDonRequestDTO request,
        HttpSession session
) {

    try {

        TruSo current =
                (TruSo) session.getAttribute("currentTruSo");

        if (current == null) {

            return ResponseEntity.status(401)
                    .body("Lỗi: Phiên đăng nhập hết hạn hoặc chưa đăng nhập.");
        }

        HoaDonResponseDTO response =
                hoaDonService.taoHoaDon(
                        request,
                        current.getId()
                );

        messagingTemplate.convertAndSend(
                "/topic/truso/" + current.getId(),
                response
        );

        if (response.getUserId() != null) {

            messagingTemplate.convertAndSend(
                    "/topic/user/" + response.getUserId() + "/invoice",
                    response
            );
        }

        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {

        return ResponseEntity.badRequest()
                .body(e.getMessage());

    } catch (Exception e) {

        return ResponseEntity.status(500)
                .body("Lỗi hệ thống: " + e.getMessage());
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

        // 2. Tìm tất cả hóa đơn dựa trên trusoId của người đang đăng nhập
        // Bạn nên sắp xếp theo ID giảm dần để hóa đơn mới nhất hiện lên đầu
        List<HoaDon> danhSach = hoaDonRepository.findByTrusoIdOrderByIdDesc(current.getId());

        return ResponseEntity.ok(danhSach);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
    }
}
}