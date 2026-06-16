package com.example.suco.controller.suco.baocao.user;

import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoItemResponseDTO; // ✅ Thêm import này
import com.example.suco.service.suco.baocao.user.TheoDoiBaoCaoService;
import com.example.suco.service.xacthuc.user.token.FirebaseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/su-co")
public class TheoDoiBaoCaoController {

    @Autowired
    private TheoDoiBaoCaoService theoDoiBaoCaoService;

    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("/theo-doi")
    public List<TheoDoiSuCoItemResponseDTO> layDanhSach(
            @RequestHeader("Authorization") String authHeader
    ) throws Exception {

        String uid = firebaseService.extractUid(authHeader);

        return theoDoiBaoCaoService.layDanhSachItem(uid);
    }

    @GetMapping("/theo-doi/{id}")
    public TheoDoiSuCoDetailResponseDTO layChiTiet(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) throws Exception {

        firebaseService.extractUid(authHeader);

        return theoDoiBaoCaoService.layChiTiet(id);
    }
}