package com.example.suco.controller.suco.baocao.admin;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; 
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
import com.example.suco.dto.suco.baocao.admin.AdminSuCoDetailResponseDTO; 
import com.example.suco.mapper.SuCoMapper; 
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.Spam;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.repository.suco.baocao.SuCoAdminRepository;
import com.example.suco.repository.suco.baocao.SpamRepository;

@Controller
@RequestMapping("/admin/bao-cao-su-co")
public class BaoCaoSuCoAdminController {

    @Autowired
    private SuCoAdminRepository reportRepository;

    @Autowired
    private SpamRepository spamRepository;

    @Autowired
    private AdminBaoCaoService adminBaoCaoService;

    @Autowired
    private SuCoMapper suCoMapper; 

    @Autowired
    private SimpMessagingTemplate messagingTemplate; 

    @GetMapping
    public String page(Model model) {
        List<BaoCaoSuCo> allSuCo = reportRepository.findAllForAdminDashboard();
        List<Spam> listSpam = spamRepository.findAll();

        long countTiepNhan = allSuCo.stream()
                .filter(s -> s.getTrangThaiXuLy() == TrangThaiXuLy.DA_TIEP_NHAN)
                .count();

        long countDiChuyen = allSuCo.stream()
                .filter(s -> s.getTrangThaiXuLy() == TrangThaiXuLy.DANG_DI_CHUYEN)
                .count();

        long countDang = allSuCo.stream()
                .filter(s -> s.getTrangThaiXuLy() == TrangThaiXuLy.DANG_XU_LY)
                .count();

        long countXong = allSuCo.stream()
                .filter(s -> s.getTrangThaiXuLy() == TrangThaiXuLy.HOAN_THANH)
                .count();

        long countHuy = allSuCo.stream()
                .filter(s -> s.getTrangThaiXuLy() == TrangThaiXuLy.HUY_BO)
                .count();

        model.addAttribute("allSuCo", allSuCo);
        model.addAttribute("listSpam", listSpam);
        
        model.addAttribute("countTiepNhan", countTiepNhan);
        model.addAttribute("countDiChuyen", countDiChuyen);
        model.addAttribute("countDang", countDang);
        model.addAttribute("countXong", countXong);
        model.addAttribute("countHuy", countHuy);
        
        model.addAttribute("activePage", "bao-cao-su-co");

        return "admin/bao-cao-su-co";
    }

    @PostMapping(value = "/admin-submit", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<AdminSuCoDetailResponseDTO> adminSubmit( 
        @ModelAttribute BaoCaoSuCo report,
        @RequestParam("image") MultipartFile image
    ) {
        // 1. Lưu xuống cơ sở dữ liệu qua Service
        BaoCaoSuCo saved = adminBaoCaoService.submitAdminReport(report, image);
        
        // 2. Chuyển đổi sang cấu hình DTO mà Frontend Javascript đang chờ đợi
        AdminSuCoDetailResponseDTO adminDto = suCoMapper.toAdminDetailDto(saved);
        
        // 3. KÍCH HOẠT WEBSOCKET: Phát tín hiệu realtime đến phòng điều khiển
        System.out.println("===> [WEBSOCKET] Đang bắn dữ liệu sự cố ID " + adminDto.getId() + " lên Web Admin...");
        messagingTemplate.convertAndSend("/topic/su-co", adminDto);
        
        return ResponseEntity.ok(adminDto); 
    }
}