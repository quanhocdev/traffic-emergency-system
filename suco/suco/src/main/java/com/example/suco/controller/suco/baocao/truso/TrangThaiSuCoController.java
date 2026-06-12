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
import java.util.List;
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
    

@GetMapping("/danh-sach-hien-tai")
public List<TruSoSuCoDetailResponseDTO> getSuCoHienTai(
        @RequestParam(required = false) String status,
        HttpSession session) {

    TruSo current = (TruSo) session.getAttribute("currentTruSo");

    if (current == null) {
        return List.of();
    }

    List<BaoCaoSuCo> data;

    if ("CHO_XU_LY".equalsIgnoreCase(status)) {
        data = repo.findPendingByTruSo(current.getId());

    } else if ("DANG_XU_LY".equalsIgnoreCase(status)) {
        data = repo.findActiveByTruSo(current.getId());

    } else {
        data = repo.findActiveByTruSo(current.getId());
    }

    return data.stream()
            .map(suCoMapper::toTruSoDetailDto)
            .toList();
}

    @GetMapping("/lich-su")
public List<TruSoSuCoDetailResponseDTO> getSuCoHistory(
        HttpSession session
) {
    TruSo current = (TruSo) session.getAttribute("currentTruSo");

    if (current == null) {
        return List.of();
    }

    return repo.findHistoryByTruSo(current.getId())
            .stream()
            .map(suCoMapper::toTruSoDetailDto)
            .toList();
}


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

    Map<String, Object> result =
            trangThaiServiceService.updateSuCoStatus(id, body.getStatus(), current);

    return ResponseEntity.ok(result);
}
@GetMapping("/chi-tiet/{id}")
public ResponseEntity<?> getChiTietSuCoChoTruSo(
        @PathVariable Long id,
        HttpSession session) {

    // 1. Kiểm tra Trụ sở đã đăng nhập chưa
    TruSo current = (TruSo) session.getAttribute("currentTruSo");
    if (current == null) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Vui lòng đăng nhập tài khoản trụ sở!"));
    }

    // 2. Tìm kiếm thực thể sự cố từ Repository của Trụ sở
    // Sử dụng repo (SuCoTruSoRepository) có sẵn trong Controller của bạn
    BaoCaoSuCo suCo = repo.findById(id).orElse(null);
    if (suCo == null) {
        return ResponseEntity.status(404)
                .body(Map.of("message", "Không tìm thấy sự cố này!"));
    }

    // 3. Map thành DTO chuẩn hiển thị đầy đủ chi tiết mà bạn đã gửi ở câu trước
    TruSoSuCoDetailResponseDTO dto = suCoMapper.toTruSoDetailDto(suCo);

    return ResponseEntity.ok(dto);
}
}