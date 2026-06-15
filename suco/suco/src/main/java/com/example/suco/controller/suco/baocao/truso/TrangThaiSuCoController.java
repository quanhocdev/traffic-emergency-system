package com.example.suco.controller.suco.baocao.truso;

import com.example.suco.dto.suco.baocao.truso.TrangThaiSuCoRequestDTO;
import com.example.suco.dto.suco.baocao.truso.TruSoSuCoDetailResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.repository.suco.baocao.SuCoTruSoRepository;
import com.example.suco.service.suco.baocao.truso.TrangThaiSuCoService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/su-co")
public class TrangThaiSuCoController {

    @Autowired
    private SuCoTruSoRepository repo;

    @Autowired
    private SuCoMapper suCoMapper;

    @Autowired
    private TrangThaiSuCoService trangThaiServiceService;

    @PatchMapping("/cap-nhat-trang-thai/{id}")
    public ResponseEntity<?> updateSuCoStatus(
            @PathVariable Long id,
            @RequestBody TrangThaiSuCoRequestDTO body,
            HttpSession session) {

        TruSo current = (TruSo) session.getAttribute("currentTruSo");

        if (current == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Vui lòng đăng nhập tài khoản trụ sở!"));
        }

        // Gọi thẳng Service và truyền vào chuỗi String thô (body.getStatus()) y hệt SOS
        Map<String, Object> result =
                trangThaiServiceService.updateSuCoStatus(id, body.getStatus(), current);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/chi-tiet/{id}")
    public ResponseEntity<?> getChiTietSuCoChoTruSo(
            @PathVariable Long id,
            HttpSession session) {

        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Vui lòng đăng nhập tài khoản trụ sở!"));
        }

        BaoCaoSuCo suCo = repo.findById(id).orElse(null);
        if (suCo == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Không tìm thấy sự cố này!"));
        }

        TruSoSuCoDetailResponseDTO dto = suCoMapper.toTruSoDetailDto(suCo);

        return ResponseEntity.ok(dto);
    }
}