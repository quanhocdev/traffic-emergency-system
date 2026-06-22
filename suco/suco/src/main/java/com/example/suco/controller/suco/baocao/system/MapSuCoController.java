package com.example.suco.controller.suco.baocao.system;

import com.example.suco.dto.suco.baocao.SuCoMapResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.service.suco.baocao.system.validation.RoleDetailService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/su-co")
public class MapSuCoController {

    @Autowired
    private BaoCaoSuCoRepository repo;

    @Autowired
private SuCoMapper suCoMapper;

@Autowired
private RoleDetailService  service;

@GetMapping("/map")
public List<SuCoMapResponseDTO> getMapData() {

return repo.findAllForAdminDashboard()
                .stream()
                .filter(s -> s.getTrangThaiXuLy() != TrangThaiXuLy.HOAN_THANH 
                          && s.getTrangThaiXuLy() != TrangThaiXuLy.HUY_BO)
                .map(suCoMapper::toMapDto)
                .toList();
    }
@GetMapping("/{id}")
public Object getDetail(@PathVariable Long id) {

    String role = SecurityContextHolder.getContext()
            .getAuthentication()
            .getAuthorities()
            .iterator()
            .next()
            .getAuthority();

    role = role.replace("ROLE_", "");

    return service.getDetail(id, role);
}
}