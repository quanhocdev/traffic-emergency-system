package com.example.suco.controller.tienich.tien.user;

import org.springframework.web.bind.annotation.RestController;

import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyDto;
import com.example.suco.model.DoiTien;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.suco.repository.tienich.tien.DoiTienRepository;
import com.example.suco.service.tienich.tien.user.DoiTienService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thong-ke-quy")
public class ThongKeQuyController {

    @Autowired
    private DoiTienService doiTienService;

    @Autowired
    private DoiTienRepository doiTienRepository;


    @GetMapping
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

    
}
