package com.example.suco.controller.api;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.BaoCaoSuCoRepository;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.DieuPhoiSOSService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/map")
public class MapSuCoController {

    @Autowired
    private BaoCaoSuCoRepository repo;

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private DieuPhoiSOSService dieuPhoiService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/su-co")
    public List<Object> getSuCoForMap(@RequestParam(value = "idTruSo", required = false) Long idTruSo) {

    List<Object> result = new ArrayList<>();

    // 1. Sự cố thường
    if (idTruSo == null || idTruSo == 0) {
        result.addAll(repo.findAllForMap());
    } else {
        result.addAll(repo.findActiveByTruSo(idTruSo));
    }

    // 2. SOS (SỬA Ở ĐÂY)
    List<TinHieuSOS> sosList;

    if (idTruSo == null || idTruSo == 0) {
        sosList = tinHieuSOSRepository.findAll().stream()
            .filter(s -> !"HOAN_THANH".equals(s.getTrangThai()) && !"HUY_BO".equals(s.getTrangThai()))
            .collect(Collectors.toList());
    } else {
        sosList = tinHieuSOSRepository.findAll().stream()
            .filter(s -> !"HOAN_THANH".equals(s.getTrangThai()) && !"HUY_BO".equals(s.getTrangThai()))
            .filter(s -> idTruSo.equals(s.getIdTruSoDeXuat()) || idTruSo.equals(s.getIdTruSoTiepNhan()))
            .collect(Collectors.toList());
    }

    result.addAll(sosList);

    return result;
    }


   
}