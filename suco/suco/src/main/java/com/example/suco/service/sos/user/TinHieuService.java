package com.example.suco.service.sos.user;

import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.DieuPhoiSOSService;
import com.example.suco.service.TinHieuSOSService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.suco.service.sos.system.notification.TinHieuRealtimeService;
import java.util.Map;

@Service
public class TinHieuService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private DieuPhoiSOSService dieuPhoiService;

    @Autowired
    private TinHieuRealtimeService tinHieuRealtimeService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
        private TinHieuSOSService tinHieuSOSService;

    



    public TinHieuSOS submitSOS(
            String uid,
            TinHieuSOSRequestDTO dto
    ) {

        Map<String, Object> ketQua =
                tinHieuSOSService.xuLyTinHieuSOS(uid, dto);

        TinHieuSOS sosDaLuu =
                (TinHieuSOS) ketQua.get("sosData");

       tinHieuRealtimeService.realtimeGuiSOS(sosDaLuu);

        return sosDaLuu;
    }
public void cancelSOS(Long id, String currentUid) {

    TinHieuSOS sos = tinHieuSOSRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Không tìm thấy SOS"));

    // check chính chủ
    if (!sos.getUserId().equals(currentUid)) {

        throw new RuntimeException(
                "Bạn không có quyền hủy yêu cầu này."
        );
    }

    // check trạng thái
    if (!"CHO_XU_LY".equals(sos.getTrangThai())) {
        throw new RuntimeException(
                "Không thể hủy vì yêu cầu đang được xử lý hoặc đã kết thúc."
        );
    }

    // cập nhật trạng thái
    sos.setTrangThai("HUY_BO");

    tinHieuSOSRepository.save(sos);

    // hủy điều phối
    dieuPhoiService.huyDieuPhoi(id);

    // realtime
    tinHieuRealtimeService.realtimeHuySOS(sos);
}
}
