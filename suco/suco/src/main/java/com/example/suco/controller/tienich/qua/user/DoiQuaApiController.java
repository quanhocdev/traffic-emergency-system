package com.example.suco.controller.tienich.qua.user;

import com.example.suco.service.tienich.qua.user.DoiQuaService;
import com.example.suco.dto.tienich.qua.quydoi.DoiQuaRequestDTO;
import com.example.suco.dto.tienich.qua.quydoi.TuiQuaResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;  
import java.util.Map;

@RestController
@RequestMapping("/api/qua")
public class DoiQuaApiController {

    @Autowired
    private DoiQuaService doiQuaService;

    // ================== 1. ĐỔI QUÀ ==================
    @PostMapping("/exchange")
public ResponseEntity<?> exchange(
        Authentication authentication,
        @RequestBody DoiQuaRequestDTO dto
) {
    try {
        String uid = authentication.getName();

        doiQuaService.thucHienDoiQua(uid, dto);

        return ResponseEntity.ok(Map.of(
                "message", "Đổi quà thành công!"
        ));

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage()
        ));

    } catch (Exception e) {
        return ResponseEntity.status(401).body(Map.of(
                "message", "Xác thực thất bại"
        ));
    }
}

    // ================== 2. TÚI QUÀ CỦA TÔI ==================
 @GetMapping("/my-gifts")
public ResponseEntity<?> getMyGifts( Authentication authentication) {
    try {
        // 1. Trích xuất UID từ header (giữ nguyên hàm helper của bạn)
        String uid = authentication.getName();

        // 2. Gọi Service để xử lý tác vụ
        List<TuiQuaResponseDTO> result = doiQuaService.getMyGifts(uid);

        // 3. Trả về kết quả
        return ResponseEntity.ok(result);

    } catch (Exception e) {
        return ResponseEntity.status(401).body("Xác thực thất bại");
    }
}
}