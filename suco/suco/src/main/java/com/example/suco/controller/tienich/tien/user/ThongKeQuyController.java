package com.example.suco.controller.tienich.tien.user;

import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyResponseDTO;
import com.example.suco.dto.tienich.tien.quydoi.DoiTienResponseDTO;
import com.example.suco.service.tienich.tien.user.DoiTienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/thong-ke-quy")
public class ThongKeQuyController {

    @Autowired
    private DoiTienService doiTienService;

    @GetMapping
    public ResponseEntity<ThongKeQuyResponseDTO> getThongKeQuy() {

        return ResponseEntity.ok(
                doiTienService.getThongKeQuy()
        );
    }

    @GetMapping("/lich-su/all")
    public ResponseEntity<List<DoiTienResponseDTO>> getAllLichSu(
            @RequestParam(required = false) String loai
    ) {

        return ResponseEntity.ok(
                doiTienService.getAllLichSu(loai)
        );
    }
}