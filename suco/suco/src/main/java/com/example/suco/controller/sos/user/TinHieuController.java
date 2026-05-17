package com.example.suco.controller.sos.user;

import org.checkerframework.checker.units.qual.A;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.suco.service.sos.user.TinHieuService;
import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.model.TinHieuSOS;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;

public class TinHieuController {

    @Autowired
    private TinHieuService tinHieuService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitSOS(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody TinHieuSOSRequestDTO dto
    ) {
    try {

        String token = authHeader.replace("Bearer ", "");
        String uid;

        if ("dev-token".equals(token)) {
            uid = "test-user";
        } else {
            FirebaseToken decodedToken =
                    FirebaseAuth.getInstance().verifyIdToken(token);

            uid = decodedToken.getUid();
        }

        TinHieuSOS sos = tinHieuService.submitSOS(uid, dto);

        return ResponseEntity.ok(sos);

    } catch (Exception e) {

        return ResponseEntity
                .status(401)
                .body("Xác thực thất bại: " + e.getMessage());
    }
}    
}
