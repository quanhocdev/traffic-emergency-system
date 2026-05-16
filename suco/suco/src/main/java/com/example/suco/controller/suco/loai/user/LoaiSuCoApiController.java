package com.example.suco.controller.suco.loai.user;

import com.example.suco.model.LoaiSuCo;
import com.example.suco.service.suco.loai.LoaiSuCoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loai-su-co")
public class LoaiSuCoApiController {

    @Autowired
    private LoaiSuCoService loaiSuCo;

    @GetMapping
    public List<LoaiSuCo> getAll() {
        return loaiSuCo.getLoaiSuCo();
    }
    
}
