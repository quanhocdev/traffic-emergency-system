package com.example.suco.controller.tienich.tien.user;

import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyRequestDTO;
import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyResponseDTO;
import com.example.suco.service.tienich.tien.user.ThongKeQuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vinh-danh")
public class ThongKeQuyController {

    @Autowired
    private ThongKeQuyService vinhDanhService;

    @PostMapping
    public ResponseEntity<ThongKeQuyResponseDTO> getThongKeQuy(
            @RequestBody ThongKeQuyRequestDTO dto
    ) {

        return ResponseEntity.ok(
                vinhDanhService.getThongKe(dto)
        );
    }
}