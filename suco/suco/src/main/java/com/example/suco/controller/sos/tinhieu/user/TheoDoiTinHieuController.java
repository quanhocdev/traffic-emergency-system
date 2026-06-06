package com.example.suco.controller.sos.tinhieu.user;

import com.example.suco.dto.sos.tinhieu.TheoDoiSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.TheoDoiSOSItemResponseDTO;
import com.example.suco.service.sos.tinhieu.user.TheoDoiTinHieuService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.suco.service.xacthuc.user.token.FirebaseService;

import java.util.List;

@RestController
@RequestMapping("/api/sos")
public class TheoDoiTinHieuController {

    @Autowired
    private TheoDoiTinHieuService theoDoiTinHieuService;

    @Autowired
    private FirebaseService firebaseService;

   @GetMapping("/theo-doi")
public List<TheoDoiSOSItemResponseDTO> layDanhSach(
        @RequestHeader("Authorization") String authHeader
) throws Exception {

    String uid = firebaseService.extractUid(authHeader);

    return theoDoiTinHieuService.layDanhSachItem(uid);
}

@GetMapping("/theo-doi/{id}")
public TheoDoiSOSDetailResponseDTO layChiTiet(
        @PathVariable Long id,
        @RequestHeader("Authorization") String authHeader
) throws Exception {

    firebaseService.extractUid(authHeader);

    return theoDoiTinHieuService.layChiTiet(id);
}
}