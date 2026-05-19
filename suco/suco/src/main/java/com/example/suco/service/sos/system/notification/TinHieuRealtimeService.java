package com.example.suco.service.sos.system.notification;

import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.dto.sos.TinHieuSOSResponseDTO;
import com.example.suco.service.sos.system.mapper.TinHieuMapper;

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
                        "message", "Bạn đã hủy yêu cầu cứu hộ"
                )
        );

        messagingTemplate.convertAndSend(
                "/topic/user/" + sos.getUserId() + "/history",
                "REFRESH"
        );
    }
}