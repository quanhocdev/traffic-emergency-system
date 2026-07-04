
package com.example.suco.controller.suco.baocao.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.example.suco.service.suco.baocao.user.GuiBaoCaoService;
import com.example.suco.service.suco.baocao.user.HuyBaoCaoService;
import jakarta.validation.Valid;

import com.example.suco.dto.suco.baocao.ai.AiResponse;
import com.example.suco.dto.suco.baocao.user.SuCoRequestDTO;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/su-co")
public class BaoCaoSuCoApiController {

    @Autowired
    private GuiBaoCaoService userBaoCaoService;

    @Autowired
    private HuyBaoCaoService huyBaoCaoService;

 @PostMapping
public ResponseEntity<?> submitReport(
        Authentication authentication,
        @Valid @RequestBody SuCoRequestDTO request
) {
    try {

        String uid = authentication.getName();

        AiResponse response =
                userBaoCaoService.submitReport(
                        uid,
                        request,
                        request.getHinhAnhUrl()
                );

        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {

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
        Authentication authentication,
        @PathVariable Long id
) {
    try {

        String currentUid = authentication.getName();

        return huyBaoCaoService.cancelReport(id, currentUid);

    } catch (Exception e) {

        return ResponseEntity.status(401)
                .body(Map.of("message", "Xác thực thất bại"));
    }
}

}