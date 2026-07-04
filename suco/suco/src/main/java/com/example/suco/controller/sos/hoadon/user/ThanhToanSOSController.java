package com.example.suco.controller.sos.hoadon.user;

import com.example.suco.dto.sos.hoadon.payment.ThanhToanRequestDTO;
import com.example.suco.dto.sos.hoadon.payment.ThanhToanResponseDTO;
import com.example.suco.service.sos.hoadon.user.HoaDonService;
import com.example.suco.service.sos.hoadon.user.ThanhToanCuuHoService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Map;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(origins = "*")
public class ThanhToanSOSController {

    @Autowired 
    private ThanhToanCuuHoService thanhToanSOSService;

    @Autowired
private HoaDonService hoaDonService;

    
    @Autowired 
    private SimpMessagingTemplate messagingTemplate;



   @GetMapping("/user/danh-sach")
public ResponseEntity<?> getHoaDonCuaUser(@RequestHeader("Authorization") String authHeader) {
    return ResponseEntity.ok(hoaDonService.getHoaDonUser(authHeader));
}
@GetMapping("/user/{id}")
public ResponseEntity<?> getChiTiet(@PathVariable Long id,
                                    @RequestHeader("Authorization") String authHeader) {
    return ResponseEntity.ok(hoaDonService.getChiTietHoaDon(id, authHeader));
}
@GetMapping("/user/{hoaDonId}/thanh-toan")
public ResponseEntity<?> getChiTietThanhToan(
        @PathVariable Long hoaDonId,
        @RequestHeader("Authorization") String authHeader
) {
    return ResponseEntity.ok(
            thanhToanSOSService.getChiTietThanhToan(
                    hoaDonId,
                    authHeader
            )
    );
}
@PostMapping("/xac-nhan")
@Transactional
public ResponseEntity<?> xacNhanThanhToan(
            Authentication authentication,
            @RequestBody ThanhToanRequestDTO request
) {
    try {
        String uid = authentication.getName();

        // 2. Xử lý thanh toán dịch vụ
        ThanhToanResponseDTO response = thanhToanSOSService.thanhToanHoaDon(uid, request);

        // Kênh thông báo cho Trụ sở cứu hộ (Giữ nguyên phát Broadcast công cộng)
        messagingTemplate.convertAndSend(
                "/topic/truso/" + response.getTrusoId(),
                response
        );

        // Ẩn danh hóa kênh thông báo Hóa đơn thành công
        messagingTemplate.convertAndSendToUser(
                uid,               // UID người nhận
                "/queue/invoice",  // Đường dẫn tĩnh phụ
                response
        );

        // Ẩn danh hóa kênh làm mới Lịch sử
        messagingTemplate.convertAndSendToUser(
                uid,               // UID người nhận
                "/queue/history",  // Đường dẫn tĩnh phụ
                "REFRESH"
        );

        return ResponseEntity.ok(response);

    }  catch (Exception e) {
        return ResponseEntity.status(500)
                .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
    }
}
}