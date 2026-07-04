package com.example.suco.controller.sos.tinhieu.user;

import com.example.suco.dto.sos.tinhieu.user.TheoDoiSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.user.TheoDoiSOSItemResponseDTO;
import com.example.suco.service.sos.tinhieu.user.TheoDoiTinHieuService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/sos")
public class TheoDoiTinHieuController {

    @Autowired
    private TheoDoiTinHieuService theoDoiTinHieuService;


   @GetMapping("/theo-doi")
public List<TheoDoiSOSItemResponseDTO> layDanhSach(
        Authentication authentication
) {

    String uid = authentication.getName();

    return theoDoiTinHieuService.layDanhSachItem(uid);
}

@GetMapping("/theo-doi/{id}")
public TheoDoiSOSDetailResponseDTO layChiTiet(
        @PathVariable Long id,
        Authentication authentication
) 
{
    return theoDoiTinHieuService.layChiTiet(id, authentication.getName());
}
}
