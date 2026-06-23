package com.example.suco.controller.vanhanh.truso;

import com.example.suco.dto.info.truso.TruSoMapDto;
import com.example.suco.service.xacthuc.truso.TruSoService;

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