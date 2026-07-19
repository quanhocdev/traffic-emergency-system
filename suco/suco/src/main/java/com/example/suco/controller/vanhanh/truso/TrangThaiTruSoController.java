package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.suco.model.TruSo;
import com.example.suco.service.vanhanh.truso.TruSoConfigService;
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
            @AuthenticationPrincipal Jwt jwt) {

        Long truSoId = Long.parseLong(jwt.getSubject());

        TruSo truSo = truSoConfigService.updateConfig(truSoId, dto);

        return ResponseEntity.ok(Map.of(
                "message", "Cập nhật thành công",
                "trangThaiHoatDong", truSo.getTrangThaiHoatDong()
        ));
    }
}