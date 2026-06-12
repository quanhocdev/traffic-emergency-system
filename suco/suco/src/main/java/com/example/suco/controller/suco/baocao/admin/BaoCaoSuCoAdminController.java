package com.example.suco.controller.suco.baocao.admin;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.example.suco.service.suco.baocao.admin.AdminBaoCaoService;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.Spam;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.repository.suco.baocao.SpamRepository;

@Controller
@RequestMapping("/admin/bao-cao-su-co")
public class BaoCaoSuCoAdminController {


    @Autowired
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private SpamRepository spamRepository;

    @Autowired
    private AdminBaoCaoService adminBaoCaoService;

    @GetMapping
public String page(Model model) {


    List<BaoCaoSuCo> allSuCo = reportRepository.findAllForMapEntity();

    List<Spam> listSpam = spamRepository.findAll();

    long countCho = allSuCo.stream()
            .filter(s -> "CHO_XU_LY".equals(s.getTrangThaiXuLy()))
            .count();

    long countDang = allSuCo.stream()
            .filter(s -> "DANG_XU_LY".equals(s.getTrangThaiXuLy()))
            .count();

    long countXong = allSuCo.stream()
            .filter(s -> "HOAN_THANH".equals(s.getTrangThaiXuLy()))
            .count();

    model.addAttribute("allSuCo", allSuCo);
    model.addAttribute("listSpam", listSpam);
    model.addAttribute("countCho", countCho);
    model.addAttribute("countDang", countDang);
    model.addAttribute("countXong", countXong);
    model.addAttribute("activePage", "bao-cao-su-co");

    return "admin/bao-cao-su-co";
}

    @PostMapping(value = "/admin-submit", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<BaoCaoSuCo> adminSubmit( // Đổi String thành BaoCaoSuCo
        @ModelAttribute BaoCaoSuCo report,
        @RequestParam("image") MultipartFile image
    ) {
        //BaoCaoSuCo saved = reportService.submitAdminReport(report, image);
        BaoCaoSuCo saved = adminBaoCaoService.submitAdminReport(report, image);
    
        return ResponseEntity.ok(saved); 
    }


}
