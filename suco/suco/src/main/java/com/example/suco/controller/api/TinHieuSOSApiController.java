        package com.example.suco.controller.api;

        import com.example.suco.service.DieuPhoiSOSService;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;
        import java.util.*;

        @RestController
        @RequestMapping("/api/tin-hieu-sos")
        @CrossOrigin(origins = "*")
        public class TinHieuSOSApiController {


            @Autowired
            private DieuPhoiSOSService dieuPhoiService;

            @GetMapping("/dieu-phoi/{idSos}")
            public ResponseEntity<?> layThongTinDieuPhoi(@PathVariable Long idSos) {
                return dieuPhoiService.layThongTinDieuPhoi(idSos)
                    .map(dieuPhoi -> {
                        Map<String, Object> ketQua = new HashMap<>();
                        ketQua.put("idSos", dieuPhoi.getIdSos());
                        ketQua.put("trangThaiDieuPhoi", dieuPhoi.getTrangThaiDieuPhoi());
                        ketQua.put("chiMucTruSoHienTai", dieuPhoi.getChiMucTruSoHienTai());
                        ketQua.put("danhSachIdTruSo", dieuPhoi.getDanhSachIdTruSo());
                        ketQua.put("danhSachKhoangCach", dieuPhoi.getDanhSachKhoangCach());
                        ketQua.put("thoiGianGuiTinCuoi", dieuPhoi.getThoiGianGuiTinCuoi());
                        
                        // Tính thời gian còn lại (giây)
                        long giayDaQua = java.time.Duration.between(
                            dieuPhoi.getThoiGianGuiTinCuoi(), 
                            java.time.LocalDateTime.now()
                        ).getSeconds();
                        long thoiGianConLai = Math.max(0, 60 - giayDaQua);
                        ketQua.put("thoiGianConLai", thoiGianConLai);
                        
                        return ResponseEntity.ok(ketQua);
                    })
                    .orElse(ResponseEntity.notFound().build());
            }

        }

