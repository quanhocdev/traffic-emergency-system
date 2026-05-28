package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.suco.model.TruSo;
import com.example.suco.service.vanhanh.truso.TruSoConfigService;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.suco.dto.truso.TruSoConfigDTO;

@Controller
@RequestMapping("/truso")
public class TrangThaiTruSoController {

    @Autowired
    private TruSoConfigService truSoConfigService;
   
@PatchMapping("/config")
@ResponseBody
public ResponseEntity<?> updateConfig(
        @RequestBody TruSoConfigDTO dto,
        HttpSession session) {

    TruSo current =
            (TruSo) session.getAttribute("currentTruSo");

    if (current == null) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Chưa đăng nhập"));
    }

    TruSo truSo =
            truSoConfigService.updateConfig(
                    current.getId(),
                    dto);

    return ResponseEntity.ok(Map.of(
            "message", "Cập nhật thành công",
            "trangThaiHoatDong", truSo.getTrangThaiHoatDong()
    ));
}
}