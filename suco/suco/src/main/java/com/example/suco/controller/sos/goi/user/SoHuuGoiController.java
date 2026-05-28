package com.example.suco.controller.sos.goi.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.suco.dto.sos.goi.payment.MuaGoiRequestDTO;
import com.example.suco.service.sos.goi.admin.CRUDGoiService;
import com.example.suco.service.sos.goi.user.SoHuuGoiService;
import com.example.suco.service.xacthuc.user.token.FirebaseService;

import java.util.Map;

@RestController
@RequestMapping("/api/mua-goi")
public class SoHuuGoiController {

    @Autowired
    private SoHuuGoiService muaGoiService;

    @Autowired
    private CRUDGoiService goiService;

    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("/danh-sach")
    public ResponseEntity<?> getDanhSachGoi() {
        return ResponseEntity.ok(goiService.getAllGoi());
    }

    // ĐĂNG KÝ GÓI 
    @PostMapping("/dang-ky")
    public ResponseEntity<?> dangKyMuaGoi(
            @RequestHeader("Authorization") String authHeader,
                    @RequestBody MuaGoiRequestDTO request
    ) {
        try {
            String uid = firebaseService.extractUid(authHeader);

            muaGoiService.dangKyGoi(uid, request);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Đăng ký gói thành công"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Xác thực thất bại: " + e.getMessage()));
        }
    }

    // HỦY GÓI 
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelGoi(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id
    ) {
        try {
            String uid = firebaseService.extractUid(authHeader);

            muaGoiService.huyGoi(id, uid);

            return ResponseEntity.ok(Map.of(
                    "message", "Đã hủy gói thành công"
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body("Xác thực thất bại");
        }
    }
    
    // LẤY DANH SÁCH GÓI ĐÃ MUA CỦA USER
    @GetMapping("/my-packages")
    public ResponseEntity<?> getMyPackages(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String uid = firebaseService.extractUid(authHeader);

            return ResponseEntity.ok(muaGoiService.getGoiByUserId(uid));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body("Xác thực thất bại");
        }
    }
}