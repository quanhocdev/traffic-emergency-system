package com.example.suco.controller.tienich.qua.user;

import com.example.suco.model.Qua;
import com.example.suco.service.tienich.qua.admin.QuaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qua")
public class QuaApiController {

    @Autowired
    private QuaService quaService;

    @GetMapping("/all")
    public List<Qua> getAllQua() {
        return quaService.getAllQua();
    }
    
}