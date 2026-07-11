package com.example.suco.controller.tienich.qua.user;

import com.example.suco.service.tienich.qua.admin.QuaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.suco.dto.tienich.qua.quanly.QuaResponseDTO;
import java.util.List;

@RestController
@RequestMapping("/api/qua-public")
public class QuaApiController {

    @Autowired
    private QuaService quaService;

    @GetMapping("/all")
    public List<QuaResponseDTO> getAllQua() {
        return quaService.getAllQua();
    }
    
}