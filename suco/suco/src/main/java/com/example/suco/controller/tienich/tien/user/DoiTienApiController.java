package com.example.suco.controller.tienich.tien.user;

import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyDto;
import com.example.suco.dto.tienich.tien.quydoi.DoiTienDto;
import com.example.suco.model.DoiTien;
import com.example.suco.repository.tienich.tien.DoiTienRepository;
import com.example.suco.service.tienich.tien.user.DoiTienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.*;

@RestController
@RequestMapping("/api/doi-tien")
public class DoiTienApiController {

    @Autowired private DoiTienService doiTienService;
    @Autowired private DoiTienRepository doiTienRepository;


   @PostMapping("/thuc-hien")
public ResponseEntity<?> thucHienDoi(
        Authentication authentication,
        @RequestBody DoiTienDto dto
) {
    try {
        String uid = authentication.getName();

        doiTienService.thucHienDoiTien(uid, dto);

        return ResponseEntity.ok(Map.of(
                "message", "Đổi tiền thành công!"
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

    @GetMapping("/thong-ke-quy")
public ResponseEntity<ThongKeQuyDto> getThongKeQuy() {
    Long tongGiaTri = doiTienRepository.sumAllDonationValues();
    if (tongGiaTri == null) tongGiaTri = 0L;

    List<Map<String, Object>> vinhDanh = doiTienService.getFormattedVinhDanh();

    return ResponseEntity.ok(new ThongKeQuyDto(tongGiaTri, vinhDanh));
}

@GetMapping("/lich-su/all")
public ResponseEntity<List<DoiTien>> getAllLichSu(@RequestParam(required = false) String loai) {
    // Gọi trực tiếp service để xử lý logic tìm kiếm toàn bộ
    return ResponseEntity.ok(doiTienService.getAllLichSu(loai));
}

@GetMapping("/lich-su")
public ResponseEntity<?> getLichSu(
        Authentication authentication,
        @RequestParam(required = false) String loai
) {
    try {
        // 1. Lấy UID từ Tokena
        String uid = authentication.getName();

        // 2. Gọi Service xử lý nghiệp vụ
        List<DoiTien> result = doiTienService.getLichSu(uid, loai);

        // 3. Trả về dữ liệu
        return ResponseEntity.ok(result);

    } catch (Exception e) {
        return ResponseEntity.status(401).body("Xác thực thất bại");
    }
}
}