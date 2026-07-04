package com.example.suco.controller.sos.tinhieu.user;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.suco.dto.sos.tinhieu.user.TinHieuSOSRequestDTO;
import com.example.suco.service.sos.tinhieu.user.GuiTinHieuService;
import com.example.suco.service.sos.tinhieu.user.HuyTinHieuService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/tin-hieu-sos")
public class TinHieuSOSController {

    @Autowired
    private GuiTinHieuService tinHieuService;

    @Autowired
    private HuyTinHieuService tinHieuHuyService;

@PostMapping("/submit")
public ResponseEntity<?> submitSOS(
        Authentication authentication,
        @RequestBody TinHieuSOSRequestDTO dto
) {
    try {

        String uid = authentication.getName();

        Long sosId = tinHieuService.submitSOS(uid, dto);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "sosId", sosId,
                        "message", "Gửi SOS thành công"
                )
        );

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity
                .status(500)
                .body("Lỗi hệ thống: " + e.getMessage());
    }
}
@PostMapping("/cancel/{id}")
public ResponseEntity<?> cancelSOS(
        Authentication authentication,
        @PathVariable Long id
) {
    try {
        String currentUid = authentication.getName();
        
        tinHieuHuyService.cancelSOS(id, currentUid);
        return ResponseEntity.ok(
                Map.of("message", "Đã hủy yêu cầu SOS thành công")
        );
    } catch (Exception e) {
        return ResponseEntity
                .status(401)
                .body("Xác thực thất bại");
    }
}

}
