package com.example.suco.controller.suco.baocao.user;

import com.example.suco.dto.suco.baocao.TheoDoiSuCoDetailResponseDTO;
import com.example.suco.service.suco.baocao.user.TheoDoiBaoCaoService;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.suco.service.xacthuc.user.token.FirebaseService;
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
    public List<TheoDoiSuCoDetailResponseDTO> layDanhSach(
            @RequestHeader("Authorization") String authHeader
    ) throws Exception {

        String uid = firebaseService.extractUid(authHeader);

        return theoDoiBaoCaoService.layDanhSach(uid);
    }
}