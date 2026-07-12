package com.example.suco.controller.tienich.tien.user;


import com.example.suco.dto.tienich.tien.quydoi.GiaoDichResultDTO;
import com.example.suco.dto.tienich.tien.quyengop.QuyenGopRequestDTO;
import com.example.suco.dto.tienich.tien.quyengop.QuyenGopResponseDTO;
import com.example.suco.service.tienich.tien.user.QuyenGopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;



@RestController
@RequestMapping("/api/quyen-gop")
public class QuyenGopController {

    @Autowired
    private QuyenGopService quyenGopService;

    @PostMapping("/thuc-hien")
public ResponseEntity<?> thucHienQuyenGop(
        Authentication authentication,
        @RequestBody QuyenGopRequestDTO dto
) {
    String uid = authentication.getName();

    boolean result = quyenGopService.thucHienQuyenGop(uid, dto);

    if (result) {
        return ResponseEntity.ok(
                new GiaoDichResultDTO(true, "Quyên góp thành công")
        );
    }

    return ResponseEntity.badRequest().body(
            new GiaoDichResultDTO(false, "Không đủ điểm hoặc dữ liệu không hợp lệ")
    );
}




    @GetMapping("/lich-su")
    public ResponseEntity<List<QuyenGopResponseDTO>> getLichSu(
            Authentication authentication
    ){

        String uid =
                authentication.getName();


        return ResponseEntity.ok(
                quyenGopService.getLichSu(uid)
        );
    }

}