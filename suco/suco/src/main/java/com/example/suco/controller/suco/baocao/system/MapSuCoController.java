package com.example.suco.controller.suco.baocao.system;

import com.example.suco.dto.suco.baocao.SuCoMapResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;

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
public List<SuCoMapResponseDTO> getMapData(
        HttpSession session,
        @RequestHeader(value = "Role", defaultValue = "USER") String role
) {

    List<BaoCaoSuCo> list;

    if ("TRU_SO".equals(role)) {

        TruSo current =
                (TruSo) session.getAttribute("currentTruSo");

        list = repo.findActiveByTruSo(current.getId());

    } else {

        list = repo.findAllForMapEntity();
    }

    return list.stream()
        .map(suCoMapper::toMapDto)
        .toList();
}
}