package com.example.suco.controller.sos.tinhieu.admin;

import com.example.suco.dto.sos.tinhieu.admin.AdminSOSDetailResponseDTO;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.repository.sos.tinhieu.AdminTinHieuSOSRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/quan-ly-sos")
public class CRUDSosController {

    @Autowired
    private AdminTinHieuSOSRepository adminTinHieuSOSRepository; 

    @Autowired
    private TinHieuMapper tinHieuMapper;

    @GetMapping
    public String hienThiQuanLySos(
            @RequestParam(name = "tab", defaultValue = "tat-ca-active") String tab,
            Model model
    ) {
        model.addAttribute("activePage", "quan-ly-sos");
        model.addAttribute("currentTab", tab);

        List<TinHieuSOS> danhSachEntity;
        
        switch (tab) {
            case "tiep-nhan":
                danhSachEntity = adminTinHieuSOSRepository.findAdminNewAssigned();
                break;
            case "di-chuyen":
                danhSachEntity = adminTinHieuSOSRepository.findAdminMoving();
                break;
            case "dang-xu-ly":
                danhSachEntity = adminTinHieuSOSRepository.findAdminActive();
                break;
            case "hoan-thanh":
                danhSachEntity = adminTinHieuSOSRepository.findAdminHistory();
                break;
            case "huy-bo":
                danhSachEntity = adminTinHieuSOSRepository.findAdminCancel();
                break;
            case "tat-ca-active":
            default:
                danhSachEntity = adminTinHieuSOSRepository.findAdminAllActive();
                break;
        }

        // Map Entity -> DTO hiển thị ra giao diện Admin HTML
        List<AdminSOSDetailResponseDTO> dtoList = danhSachEntity.stream()
                .map(sos -> tinHieuMapper.toAdminDetailDto(sos, null))
                .collect(Collectors.toList());

        model.addAttribute("list", dtoList);
        model.addAttribute("cacTrangThai", TrangThaiXuLy.values());

        return "admin/quan-ly-sos";
    }
}