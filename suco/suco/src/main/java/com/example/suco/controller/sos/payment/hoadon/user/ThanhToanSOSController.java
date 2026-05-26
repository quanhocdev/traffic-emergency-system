package com.example.suco.controller.sos.payment.hoadon.user;

import com.example.suco.repository.payment.HoaDonRepository;
import com.example.suco.service.sos.payment.hoadon.truso.HoaDonSOSService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.example.suco.dto.sos.payment.hoadon.request.ThanhToanRequestDTO;
import com.example.suco.dto.sos.payment.hoadon.response.ThanhToanResponseDTO;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(origins = "*")
public class ThanhToanSOSController {

    @Autowired 
    private HoaDonSOSService hoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired 
    private SimpMessagingTemplate messagingTemplate;


@PostMapping("/xac-nhan/{id}")
@Transactional
public ResponseEntity<?> xacNhanThanhToan(
    @RequestHeader("Authorization") String authHeader,
    @RequestBody ThanhToanRequestDTO request
) {
    try {
        // 1. Lấy UID từ token
        String token = authHeader.replace("Bearer ", "");
        String uid;

        if ("dev-token".equals(token)) {
            uid = "test-user";
        } else {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            uid = decodedToken.getUid();
        }

        // 2. Tìm hóa đơn
        return hoaDonRepository.findById(request.getHoaDonId()).map(hd -> {

            // 3. Check chính chủ
            if (hd.getUserId() == null || !hd.getUserId().equals(uid)) {
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Bạn không có quyền thanh toán hóa đơn này"));
            }

            // 4. Check trạng thái (tránh thanh toán lại)
            if ("PAID".equalsIgnoreCase(hd.getTrangThai())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Hóa đơn đã được thanh toán trước đó"));
            }

            // 5. Áp dụng voucher nếu có
            if (request.getQuaId() != null) {
                hoaDonService.apDungVoucherChoHoaDon(hd, request.getQuaId());
            }

            // 6. Cập nhật trạng thái
            hd.setTrangThai("PAID");
            hoaDonRepository.save(hd);

            // 7. Response trả về
            ThanhToanResponseDTO response =
        new ThanhToanResponseDTO();

response.setHoaDonId(hd.getId());
response.setTrangThai("PAID");
response.setTongThanhToan(hd.getTongThanhToan());
response.setMessage("Thanh toán thành công");

            // 8. Realtime cho trụ sở
            messagingTemplate.convertAndSend(
                "/topic/truso/" + hd.getTrusoId(),
                response
            );

            // 9. Realtime cho user
            messagingTemplate.convertAndSend(
                "/topic/user/" + uid + "/invoice",
                response
            );

            messagingTemplate.convertAndSend(
                "/topic/user/" + uid + "/history",
                "REFRESH"
            );

            return ResponseEntity.ok(response);

        }).orElse(ResponseEntity.notFound().build());

    } catch (FirebaseAuthException e) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Token không hợp lệ"));
    } catch (Exception e) {
        return ResponseEntity.status(500)
                .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
    }
}
}