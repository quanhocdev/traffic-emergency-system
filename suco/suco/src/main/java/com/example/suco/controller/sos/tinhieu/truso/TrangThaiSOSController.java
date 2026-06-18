package com.example.suco.controller.sos.tinhieu.truso;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.suco.dto.sos.tinhieu.truso.TrangThaiSOSRequestDTO;
import com.example.suco.dto.sos.tinhieu.truso.TruSoSOSDetailResponseDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.sos.tinhieu.truso.TrangThaiService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;


@RestController
@RequestMapping("/sos")
public class TrangThaiSOSController {

    @Autowired
    private TrangThaiService trangThaiSOSService;
    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
    
  @PatchMapping("/cap-nhat-trang-thai/{id}")
public ResponseEntity<?> updateStatus(
        @PathVariable Long id,
        @RequestBody TrangThaiSOSRequestDTO body,
        HttpSession session) {

    TruSo current = (TruSo) session.getAttribute("currentTruSo");

    trangThaiSOSService.capNhatTrangThaiSOS(
        id,
        body.getStatus(),
        current
    );

    return ResponseEntity.ok(
        Map.of("message", "Cập nhật thành công")
    );
}
@GetMapping("/lich-su")
public ResponseEntity<?> getSosHistory(HttpSession session) {
    TruSo current = (TruSo) session.getAttribute("currentTruSo");

    if (current == null) {
        return ResponseEntity.status(401).body("Chưa đăng nhập");
    }

    // Gọi hàm Service mới để lấy danh sách đã được map sang DTO an toàn
    List<TruSoSOSDetailResponseDTO> list = trangThaiSOSService.layLichSuSOSChoTruSo(current.getId());
    return ResponseEntity.ok(list);
}
@GetMapping("/hoat-dong")
public ResponseEntity<?> getSosActive(
        @RequestParam(required = false) String status,
        HttpSession session
) {

    TruSo current = (TruSo) session.getAttribute("currentTruSo");

    return ResponseEntity.ok(
            trangThaiSOSService.layDanhSachSOSActive(
                    current,
                    status
            )
    );
}
@GetMapping("/chi-tiet/{id}")
    public ResponseEntity<?> getChiTietSosChoTruSo(
            @PathVariable Long id,
            HttpSession session) {

        TruSo current = (TruSo) session.getAttribute("currentTruSo");

        // Gọi sang Service xử lý logic và nhận về DTO đầy đủ trường thông tin
        TruSoSOSDetailResponseDTO dto = trangThaiSOSService.layChiTietSOSChoTruSo(id, current);

        return ResponseEntity.ok(dto);
    }
}

