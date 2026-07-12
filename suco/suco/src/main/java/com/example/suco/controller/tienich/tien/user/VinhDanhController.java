package com.example.suco.controller.tienich.tien.user;


import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyResponseDTO;
import com.example.suco.service.tienich.tien.user.VinhDanhService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/vinh-danh")
public class VinhDanhController {


    @Autowired
    private VinhDanhService vinhDanhService;


    @GetMapping
    public ResponseEntity<ThongKeQuyResponseDTO> getThongKeQuy(){

        return ResponseEntity.ok(
                vinhDanhService.getThongKe()
        );
    }

}