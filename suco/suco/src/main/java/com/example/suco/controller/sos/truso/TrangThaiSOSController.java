package com.example.suco.controller.sos.truso;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.suco.model.TruSo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.suco.service.sos.truso.TrangThaiService;
import java.util.Map;


@RestController
@RequestMapping("/sos")
public class TrangThaiSOSController {

    @Autowired
    private TrangThaiService trangThaiSOSService;
    
    @PatchMapping("/cap-nhat-trang-thai/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
            @RequestParam("status") String status,
            HttpSession session) {

    TruSo current = (TruSo) session.getAttribute("currentTruSo");

        trangThaiSOSService.capNhatTrangThaiSOS(
        id,
        status,
        current
    );

    return ResponseEntity.ok(
        Map.of("message", "Cập nhật thành công")
    );
}
}
