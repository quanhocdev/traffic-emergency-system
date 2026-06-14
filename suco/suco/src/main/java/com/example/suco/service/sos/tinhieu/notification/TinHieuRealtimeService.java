package com.example.suco.service.sos.tinhieu.notification;

import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.suco.dto.sos.tinhieu.SOSMapResponseDTO;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.enums.TrangThaiXuLy;
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
                        "trangThai", TrangThaiXuLy.HUY_BO.name(),
                        "message", layThongDiepTrangThai(TrangThaiXuLy.HUY_BO)
                )
        );

        messagingTemplate.convertAndSend(
                "/topic/user/" + sos.getUserId() + "/history",
                "REFRESH"
        );
    }

    private String layThongDiepTrangThai(TrangThaiXuLy trangThai) {
        if (trangThai == null) {
            return "Yêu cầu đã gửi đi!";
        }
        
        // Dùng mẫu switch-case mới (Switch Expression) của Java cho sạch đẹp
        return switch (trangThai) {
            case CHO_ADMIN -> "Hệ thống đang tìm kiếm đội cứu hộ gần nhất...";
            case DA_TIEP_NHAN -> "Yêu cầu cứu hộ đã được tiếp nhận!";
            case DANG_DI_CHUYEN -> "Lực lượng cứu hộ đang trên đường di chuyển đến!";
            case DANG_XU_LY -> "Lực lượng cứu hộ đang tiến hành xử lý tại hiện trường!";
            case HOAN_THANH -> "Cứu hộ đã hoàn tất thành công!";
            case HUY_BO -> "Yêu cầu đã bị hủy!";
        };
    }

    public void guiThongDiep(TinHieuSOS sos) {
        String trangThaiStr = sos.getTrangThai() != null ? sos.getTrangThai().name() : "CHO_ADMIN";

        messagingTemplate.convertAndSend(
                "/topic/user/" + sos.getUserId() + "/sos-status",
                Map.of(
                        "idSOS", sos.getId(),
                        "trangThai", trangThaiStr,
                        "message", layThongDiepTrangThai(sos.getTrangThai())
                )
        );
    }
}