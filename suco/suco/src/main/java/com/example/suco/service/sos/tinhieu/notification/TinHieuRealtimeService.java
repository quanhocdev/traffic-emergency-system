package com.example.suco.service.sos.tinhieu.notification;

import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.suco.dto.sos.tinhieu.SOSMapResponseDTO;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import java.util.List;

@Service
public class TinHieuRealtimeService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

  public List<SOSMapResponseDTO> getMapData() {
    return tinHieuSOSRepository.findAll()
            .stream()
            .map(tinHieuMapper::toMapDto)
            .toList();
}

    public void realtimeGuiSOS(TinHieuSOS sos) {

    SOSMapResponseDTO dto = tinHieuMapper.toMapDto(sos);

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

    SOSMapResponseDTO dto = tinHieuMapper.toMapDto(sos);

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
        case "DA_TIEP_NHAN":
            return "Yêu cầu đã được tiếp nhận!";
        case "CHO_XU_LY":
            return "Lực lượng cứu hộ đang đến!";
        case "DANG_XU_LY":
            return "Lực lượng cứu hộ đang xử lý!";
        case "HOAN_THANH":
            return "Cứu hộ đã hoàn tất!";
        case "HUY_BO":
            return "Yêu cầu đã bị hủy!";
        default:
            return "Yêu cầu đã gửi đi!";
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