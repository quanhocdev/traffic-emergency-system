
package com.example.suco.controller.api;

import com.example.suco.dto.AiRejectResponse;
import com.example.suco.dto.LichSuDto;
import com.example.suco.dto.SuCoMapDto;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.HoaDon;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.User;
import com.example.suco.repository.BaoCaoSuCoRepository;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.repository.UserRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.example.suco.service.AiVerifyResult;
import com.example.suco.service.BaoCaoSuCoService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/su-co")
public class BaoCaoSuCoApiController {

    @Autowired
private com.example.suco.util.GeocodingUtil geocodingUtil;

    @Autowired
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BaoCaoSuCoService baoCaoSuCoService;
    @Autowired
private com.example.suco.repository.HoaDonRepository hoaDonRepository; // Thêm dòng này

@Autowired
private TinHieuSOSRepository tinHieuSOSRepository; // Thêm dòng này vào đầu Class
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<AiRejectResponse> submitReport(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BaoCaoSuCo report
    ) {
        try {
            // 1. LẤY TOKEN VÀ XỬ LÝ BYPASS
            String token = authHeader.replace("Bearer ", "");
            String uid;

            if ("dev-token".equals(token)) {
                uid = "test-user"; // UID dùng để test trên Postman
            } else {
                // Luồng thật cho App Android
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                uid = decodedToken.getUid();
            }

            // 2. TÌM USER TRONG DATABASE
            User user = userRepository.findById(uid)
                    .orElseThrow(() -> new RuntimeException("User chưa tồn tại trong hệ thống"));

            // Gán người báo cáo là User vừa tìm được
            report.setReporter(user);

            AiVerifyResult ai = baoCaoSuCoService.submitReport(uid, report, report.getHinhAnhUrl());
            if (!ai.isValid()) {

        // phân biệt message
        String code = ai.getReason().contains("trước đó") 
            ? "DUPLICATE" 
            : "AI_REJECTED";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new AiRejectResponse(code, ai.getReason(), ai.getConfidence(), ai.getDistance())
        );
    }

            // 4. NẾU HỢP LỆ -> SOCKET SẼ GỬI ĐẾN ADMIN/MAP (Logic này nằm trong service của bạn)
            return ResponseEntity.ok(
        new AiRejectResponse(
            "AI_APPROVED",
            "Báo cáo sự cố thành công",
            ai.getConfidence(),
            ai.getDistance()
        )
    );

        } catch (FirebaseAuthException e) {
        return ResponseEntity.status(401).body(
            new AiRejectResponse("UNAUTHORIZED", "Lỗi xác thực: " + e.getMessage(), 0)
        );
    }
    }

   @GetMapping("/map-data")
public List<SuCoMapDto> getAllForMap() {
    return reportRepository.findAllForMap(); 
}


@PatchMapping("/{id}")
public ResponseEntity<?> cancelReport(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable Long id
) {
    try {
        String token = authHeader.replace("Bearer ", "");
        String currentUid;

        if ("dev-token".equals(token)) {
            currentUid = "test-user";
        } else {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            currentUid = decodedToken.getUid();
        }

        return baoCaoSuCoService.cancelReport(id, currentUid);

    } catch (Exception e) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Xác thực thất bại"));
    }
}
// Sửa lại hàm convertToDto trong BaoCaoSuCoService
private SuCoMapDto convertToDto(BaoCaoSuCo b) {
    String tenLoai = (b.getLoaiSuCo() != null) ? b.getLoaiSuCo().getTen() : "Không xác định";
    String iconUrl = (b.getLoaiSuCo() != null) ? b.getLoaiSuCo().getIconUrl() : "";
    
    String tenNguoiBao = "Người dùng ẩn danh";
    if (b.getReporter() != null) {
        tenNguoiBao = b.getReporter().getName();
    }

    // ĐẢM BẢO THỨ TỰ THAM SỐ KHỚP VỚI FILE SuCoMapDto.java (17 tham số)
    return new SuCoMapDto(
        b.getId(), 
        b.getViDo(), 
        b.getKinhDo(), 
        b.getMoTa(),            // Tham số mô tả
        tenLoai,
        b.getTrangThaiDuyet(), 
        b.getTrangThaiXuLy(), 
        iconUrl,
        b.getMucDoNghiemTrong(), 
        b.getHinhAnhUrl(),
        b.getDoTinCay(),        // THÊM: doTinCay để tránh lỗi undefined
        null, null, null, null, // chi tiết địa chỉ
        b.getDiaChi(),          
        tenNguoiBao             
    );
}


}