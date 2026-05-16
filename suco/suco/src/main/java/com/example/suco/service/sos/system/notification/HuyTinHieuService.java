package com.example.suco.service.sos.system.notification;

import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.example.suco.model.TinHieuSOS;

@Service
public class HuyTinHieuService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void guiRealtimeHuySOS(TinHieuSOS sos) {

    if (sos.getIdTruSoDeXuat() != null) {

        messagingTemplate.convertAndSend(
                "/topic/tru-so/" + sos.getIdTruSoDeXuat(),
                sos
        );
    }

    if (sos.getIdTruSoTiepNhan() != null) {

        messagingTemplate.convertAndSend(
                "/topic/tru-so/" + sos.getIdTruSoTiepNhan(),
                sos
        );
    }

    messagingTemplate.convertAndSend(
            "/topic/admin",
            sos
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
