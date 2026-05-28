
package com.example.suco.controller.suco.baocao.user;

import com.example.suco.service.xacthuc.user.token.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.example.suco.service.suco.baocao.user.GuiBaoCaoService;
import com.example.suco.service.suco.baocao.user.HuyBaoCaoService;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.validation.Valid;

import com.example.suco.dto.suco.baocao.SuCoRequestDTO;
import com.example.suco.dto.suco.baocao.ai.AiResponse;

@RestController
@RequestMapping("/api/su-co")
public class BaoCaoSuCoApiController {

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private GuiBaoCaoService userBaoCaoService;

    @Autowired
    private HuyBaoCaoService huyBaoCaoService;

    @PostMapping
public ResponseEntity<?> submitReport(
        @RequestHeader("Authorization") String authHeader,
        @Valid @RequestBody SuCoRequestDTO request
) {
    try {

        String uid = firebaseService.extractUid(authHeader);

        AiResponse response =
                userBaoCaoService.submitReport(
                        uid,
                        request,
                        request.getHinhAnhUrl()
                );

        return ResponseEntity.ok(response);

    } catch (FirebaseAuthException e) {

        return ResponseEntity.status(401).body(
                new AiResponse(
                        "UNAUTHORIZED",
                        "Lỗi xác thực: " + e.getMessage(),
                        0
                )
        );
    }
}

@PatchMapping("/{id}")
public ResponseEntity<?> cancelReport(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable Long id
) {
    try {
        String currentUid = firebaseService.extractUid(authHeader);
        return huyBaoCaoService.cancelReport(id, currentUid);
    } catch (Exception e) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Xác thực thất bại"));
    }
}

}