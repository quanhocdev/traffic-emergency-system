package com.example.suco.service.sos.tinhieu.system.notification;

import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.suco.dto.sos.tinhieu.TinHieuSOSResponseDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.service.sos.tinhieu.system.mapper.TinHieuMapper;

@Service
public class TinHieuRealtimeService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    public void realtimeGuiSOS(TinHieuSOS sos) {

        TinHieuSOSResponseDTO dto = tinHieuMapper.mapToDTO(sos);

        messagingTemplate.convertAndSend(
                "/topic/admin",
                dto
        );

        messagingTemplate.convertAndSend(
                "/topic/user/" + sos.getUserId() + "/history",
                "REFRESH"
        );
    }

    public void realtimeHuySOS(TinHieuSOS sos) {

        TinHieuSOSResponseDTO dto = tinHieuMapper.mapToDTO(sos);

        if (sos.getIdTruSoDeXuat() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/tru-so/" + sos.getIdTruSoDeXuat(),
                    dto
            );
        }

        if (sos.getIdTruSoTiepNhan() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/tru-so/" + sos.getIdTruSoTiepNhan(),
                    dto
            );
        }

        messagingTemplate.convertAndSend(
                "/topic/admin",
                dto
        );

        messagingTemplate.convertAndSend(
        "/topic/user/" + sos.getUserId() + "/sos-status",
        Map.of(
                "idSOS", sos.getId(),
                "trangThai", "HUY_BO",
                "message", layThongDiepTrangThai("HUY_BO")
        )
);

        messagingTemplate.convertAndSend(
                "/topic/user/" + sos.getUserId() + "/history",
                "REFRESH"
        );
    }
    private String layThongDiepTrangThai(String trangThai) {
    switch (trangThai) {
        case "DANG_XU_LY":
            return "Lực lượng cứu hộ đang đến!";

        case "HOAN_THANH":
            return "Hỗ trợ đã hoàn tất.";

        case "HUY_BO":
            return "Yêu cầu đã bị hủy.";

        default:
            return "Yêu cầu đang được xử lý.";
    }
}
public void guiThongDiep(TinHieuSOS sos) {

    messagingTemplate.convertAndSend(
            "/topic/user/" + sos.getUserId() + "/sos-status",
            Map.of(
                    "idSOS", sos.getId(),
                    "trangThai", sos.getTrangThai(),
                    "message", layThongDiepTrangThai(sos.getTrangThai())
            )
    );
}
}