package com.example.suco.controller.suco.baocao.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.suco.service.suco.baocao.user.LichSuService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
@RequestMapping("/api/lich-su")
public class LichSuController {

    @Autowired
    private LichSuService lichSuService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String type
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String uid;

                FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
                uid = decoded.getUid();

            return ResponseEntity.ok(
                    lichSuService.getAllHistory(uid, type)
            );

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Xác thực thất bại: " + e.getMessage()));
        }
    }
}