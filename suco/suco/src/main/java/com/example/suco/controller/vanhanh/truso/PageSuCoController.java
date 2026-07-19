package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.repository.suco.baocao.SuCoTruSoRepository;
import java.util.List;

@RestController 
@RequestMapping("/truso")
public class PageSuCoController {

    @Autowired
    private SuCoTruSoRepository suCoTruSoRepository;

    private Long getTruSoId(Jwt jwt) {
        // Ép kiểu subject từ String sang Long (hoặc lấy trực tiếp claim "id" nếu bạn cấu hình lưu riêng)
        return Long.parseLong(jwt.getSubject());
    }

    @GetMapping("/api/su-co/da-tiep-nhan")
    public List<BaoCaoSuCo> getSuCoDaTiepNhan(@AuthenticationPrincipal Jwt jwt) {
        return suCoTruSoRepository.findNewAssignedByTruSo(getTruSoId(jwt));
    }

    @GetMapping("/api/su-co/dang-di-chuyen")
    public List<BaoCaoSuCo> getSuCoDangDiChuyen(@AuthenticationPrincipal Jwt jwt) {
        return suCoTruSoRepository.findPendingByTruSo(getTruSoId(jwt));
    }

    @GetMapping("/api/su-co/dang-xu-ly")
    public List<BaoCaoSuCo> getSuCoDangXuLy(@AuthenticationPrincipal Jwt jwt) {
        return suCoTruSoRepository.findActiveByTruSo(getTruSoId(jwt));
    }

    @GetMapping("/api/su-co/da-xu-ly")
    public List<BaoCaoSuCo> getSuCoDaXuLy(@AuthenticationPrincipal Jwt jwt) {
        return suCoTruSoRepository.findHistoryByTruSo(getTruSoId(jwt));
    }

    @GetMapping("/api/su-co/huy-xu-ly")
    public List<BaoCaoSuCo> getSuCoHuyXuLy(@AuthenticationPrincipal Jwt jwt) {
        return suCoTruSoRepository.findCancelByTruSo(getTruSoId(jwt));
    }
}