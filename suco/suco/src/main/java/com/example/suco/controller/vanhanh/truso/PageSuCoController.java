package com.example.suco.controller.vanhanh.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.suco.model.TruSo;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.repository.suco.baocao.SuCoTruSoRepository;
import java.util.List;
import jakarta.servlet.http.HttpSession;

@RestController 
@RequestMapping("/truso")
public class PageSuCoController {

    @Autowired
    private SuCoTruSoRepository suCoTruSoRepository;

    @GetMapping("/api/su-co/da-tiep-nhan")
    public List<BaoCaoSuCo> getSuCoDaTiepNhan(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findNewAssignedByTruSo(current.getId());
    }

    @GetMapping("/api/su-co/cho-xu-ly")
    public List<BaoCaoSuCo> getSuCoChoXuLy(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findPendingByTruSo(current.getId());
    }

    @GetMapping("/api/su-co/dang-xu-ly")
    public List<BaoCaoSuCo> getSuCoDangXuLy(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findActiveByTruSo(current.getId());
    }

    @GetMapping("/api/su-co/da-xu-ly")
    public List<BaoCaoSuCo> getSuCoDaXuLy(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");
        if (current == null) return List.of();
        return suCoTruSoRepository.findHistoryByTruSo(current.getId());
    }

}