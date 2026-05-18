package com.example.suco.controller.suco.baocao.system;

import com.example.suco.dto.SuCoMapDto;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.repository.BaoCaoSuCoRepository;
import com.example.suco.service.suco.baocao.system.mapper.SuCoMapper;
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
        @RequestParam(required = false) Long idTruSo,
        @RequestHeader("Role") String role
) {

    List<BaoCaoSuCo> list;

    if ("TRU_SO".equals(role)) {
        list = repo.findActiveByTruSo(idTruSo); // hoặc cả 2 nếu cần
    } else {
        list = repo.findAll(); // vì bạn đã map entity rồi
    }

    return list.stream()
            .map(suCoMapper::convertToDto)
            .toList();
}
}