package com.example.suco.controller.suco.baocao.system;

import com.example.suco.dto.SuCoMapDto;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.repository.BaoCaoSuCoRepository;
import com.example.suco.service.suco.baocao.system.mapper.SuCoMapper;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
@RequestMapping("/api/su-co")
public class MapSuCoController {

    @Autowired
    private BaoCaoSuCoRepository repo;

    @Autowired
    private SuCoMapper suCoMapper;

    @GetMapping("/map")
public List<SuCoMapDto> getMapData(
        HttpSession session,
        @RequestHeader(value = "Role", defaultValue = "USER") String role
) {

    List<BaoCaoSuCo> list;

    if ("TRU_SO".equals(role)) {

        TruSo current =
                (TruSo) session.getAttribute("currentTruSo");

        list = repo.findActiveByTruSo(current.getId());

    } else {

        list = repo.findAll();
    }

    return list.stream()
            .map(suCoMapper::convertToDto)
            .toList();
}
}