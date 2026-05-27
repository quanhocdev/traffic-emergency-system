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
import com.example.suco.service.sos.payment.hoadon.user.ThanhToanSOSService;
@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(origins = "*")
public class ThanhToanSOSController {

    @Autowired 
    private ThanhToanSOSService thanhToanSOSService;

    
    @Autowired 
    private SimpMessagingTemplate messagingTemplate;


@PostMapping("/xac-nhan")
@Transactional
public ResponseEntity<?> xacNhanThanhToan(
    @RequestHeader("Authorization") String authHeader,
    @RequestBody ThanhToanRequestDTO request
) {
    try {
        // 1. Lấy UID từ token
        String token = authHeader.replace("Bearer ", "");
        String uid;

            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            uid = decodedToken.getUid();
        

        // 2. Tìm hóa đơn
       ThanhToanResponseDTO response =
        thanhToanSOSService.thanhToanHoaDon(
                uid,
                request
        );

messagingTemplate.convertAndSend(
        "/topic/truso/" + response.getTrusoId(),
        response
);

messagingTemplate.convertAndSend(
        "/topic/user/" + uid + "/invoice",
        response
);

messagingTemplate.convertAndSend(
        "/topic/user/" + uid + "/history",
        "REFRESH"
);

return ResponseEntity.ok(response);

    } catch (FirebaseAuthException e) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Token không hợp lệ"));
    } catch (Exception e) {
        return ResponseEntity.status(500)
                .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
    }
}
}