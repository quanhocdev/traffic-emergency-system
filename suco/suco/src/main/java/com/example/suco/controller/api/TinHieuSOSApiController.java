        package com.example.suco.controller.api;

        import com.example.suco.dto.TinHieuSOSRequestDTO;
        import com.example.suco.model.TinHieuSOS;
        import com.example.suco.model.TruSo;
        import com.example.suco.repository.MuaGoiRepository;
        import com.example.suco.repository.TinHieuSOSRepository;
        import com.example.suco.service.DieuPhoiSOSService;
        import com.example.suco.service.DieuPhoiSOSService.ThongTinDieuPhoi;
        import com.example.suco.service.TinHieuSOSService;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseToken;

        import jakarta.servlet.http.HttpSession;

        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.messaging.simp.SimpMessagingTemplate;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;
        import java.util.*;
        import java.util.stream.Collectors;

        @RestController
        @RequestMapping("/api/tin-hieu-sos")
        @CrossOrigin(origins = "*")
        public class TinHieuSOSApiController {

            @Autowired
            private TinHieuSOSService tinHieuSOSService;

            @Autowired
            private SimpMessagingTemplate messagingTemplate;
            
            @Autowired 
            private TinHieuSOSRepository tinHieuSOSRepository;

            @Autowired
            private DieuPhoiSOSService dieuPhoiService;

            @Autowired
        private MuaGoiRepository muaGoiRepository; 



    @GetMapping("/active")
public ResponseEntity<?> getSosActive(
    @RequestParam(required = false) String status, // Thêm dòng này để nhận filter từ Postman
    HttpSession session
) {
    TruSo current = (TruSo) session.getAttribute("currentTruSo");
    if (current == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

    // 1. Lấy tất cả danh sách đang hoạt động (Chờ + Đang xử lý)
    List<TinHieuSOS> list = tinHieuSOSRepository.findActiveByTruSo(current.getId());

    // 2. Nếu Postman có truyền ?status=..., thì thực hiện lọc
    if (status != null && !status.isEmpty()) {
        list = list.stream()
                   .filter(sos -> sos.getTrangThai().equalsIgnoreCase(status))
                   .collect(Collectors.toList());
    }

    // 3. Set VIP (giữ nguyên logic cũ của bạn)
    for (TinHieuSOS sos : list) {
        boolean laVip = muaGoiRepository.findByUserId(sos.getUserId())
            .stream()
            .anyMatch(mg -> "ACTIVE".equalsIgnoreCase(mg.getTrangThai()));
        sos.setIsVip(laVip);
    }

    return ResponseEntity.ok(list);
}

            private String layThongDiepTrangThai(String trangThai) {
                switch (trangThai) {
                    case "DANG_XU_LY": return "Lực lượng cứu hộ đang đến!";
                    case "HOAN_THANH": return "Hỗ trợ đã hoàn tất.";
                    default: return "Yêu cầu đang được xử lý.";
                }
            }
        @GetMapping("/history")
    public ResponseEntity<?> getSosHistory(HttpSession session) {
        TruSo current = (TruSo) session.getAttribute("currentTruSo");

        if (current == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        List<TinHieuSOS> list = tinHieuSOSRepository.findHistoryByTruSo(current.getId());
        return ResponseEntity.ok(list);
    }


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

