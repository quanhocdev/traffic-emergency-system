package com.example.suco.controller.suco.baocao.user;

import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoItemResponseDTO; 
import com.example.suco.service.suco.baocao.user.TheoDoiBaoCaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/su-co")
public class TheoDoiBaoCaoController {

    @Autowired
    private TheoDoiBaoCaoService theoDoiBaoCaoService;


    @GetMapping("/theo-doi")
public List<TheoDoiSuCoItemResponseDTO> layDanhSach(
        Authentication authentication
) {

    String uid = authentication.getName();

    return theoDoiBaoCaoService.layDanhSachItem(uid);
}

    @GetMapping("/theo-doi/{id}")
public TheoDoiSuCoDetailResponseDTO layChiTiet(
        @PathVariable Long id,
        Authentication authentication
) {

    return theoDoiBaoCaoService.layChiTiet(
            id,
            authentication.getName()
    );
}
}