package com.example.suco.controller.api;

import com.example.suco.dto.TruSoMapDto;
import com.example.suco.service.TruSoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tru-so")
public class TruSoApiController {

    @Autowired
    private TruSoService truSoService;

    @GetMapping("/all")
    public List<TruSoMapDto> getAllTruSo() {
        return truSoService.getAllTruSoForMap();
    }
}