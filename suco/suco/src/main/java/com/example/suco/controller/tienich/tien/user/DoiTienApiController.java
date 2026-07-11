package com.example.suco.controller.tienich.tien.user;

import com.example.suco.dto.tienich.tien.quydoi.DoiTienRequestDTO;
import com.example.suco.dto.tienich.tien.quydoi.DoiTienResultDTO;
import com.example.suco.service.tienich.tien.user.DoiTienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/doi-tien")
public class DoiTienApiController {

    @Autowired private DoiTienService doiTienService;

@PostMapping("/thuc-hien")
public ResponseEntity<DoiTienResultDTO> thucHienDoi(
        Authentication authentication,
        @RequestBody DoiTienRequestDTO dto
) {
    try {
        String uid = authentication.getName();

        doiTienService.thucHienDoiTien(uid, dto);

        return ResponseEntity.ok(
                new DoiTienResultDTO(true, "Đổi tiền thành công!")
        );

    } catch (RuntimeException e) {

        return ResponseEntity.badRequest().body(
                new DoiTienResultDTO(false, e.getMessage())
        );
    }
}

    
@GetMapping("/lich-su")
public ResponseEntity<?> getLichSu(
        Authentication authentication,
        @RequestParam(required = false) String loai
) {
    try {

        String uid = authentication.getName();

        return ResponseEntity.ok(
                doiTienService.getLichSu(uid, loai)
        );

    } catch (Exception e) {
        return ResponseEntity.status(401).body("Xác thực thất bại");
    }
}
}