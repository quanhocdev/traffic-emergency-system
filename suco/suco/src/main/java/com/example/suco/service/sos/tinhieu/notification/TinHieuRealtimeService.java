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

    // 1. GỬI TÍN HIỆU SOS
    public void realtimeGuiSOS(TinHieuSOS sos) {
        SOSMapResponseDTO dto = tinHieuMapper.toMapDto(sos);

        // Kênh chung cho Admin thì giữ nguyên /topic
        messagingTemplate.convertAndSend("/topic/admin", dto);

        // SỬA TẠI ĐÂY: Dùng convertAndSendToUser để ẩn danh hóa kênh history
        messagingTemplate.convertAndSendToUser(
                sos.getUserId(),           // UID người nhận
                "/queue/history",          // Tên kênh tĩnh (bỏ /topic/user/uid)
                "REFRESH"
        );
    }

    // 2. HỦY TÍN HIỆU SOS
    public void realtimeHuySOS(TinHieuSOS sos) {
        SOSMapResponseDTO dto = tinHieuMapper.toMapDto(sos);

        if (sos.getIdTruSoTiepNhan() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/tru-so/" + sos.getIdTruSoTiepNhan(),
                    dto
            );
        }

        messagingTemplate.convertAndSend("/topic/admin", dto);

        // SỬA TẠI ĐÂY: Ẩn danh hóa kênh cập nhật trạng thái SOS
        messagingTemplate.convertAndSendToUser(
                sos.getUserId(),
                "/queue/sos-status",
                Map.of(
                        "idSOS", sos.getId(),
                        "trangThai", TrangThaiXuLy.HUY_BO.name(),
                        "message", layThongDiepTrangThai(TrangThaiXuLy.HUY_BO)
                )
        );

        // SỬA TẠI ĐÂY: Ẩn danh hóa kênh history khi hủy
        messagingTemplate.convertAndSendToUser(
                sos.getUserId(),
                "/queue/history",
                "REFRESH"
        );
    }

    // 3. CẬP NHẬT TRẠNG THÁI TIẾN ĐỘ CỨU HỘ ĐANG ĐI DI CHUYỂN...
    public void guiThongDiep(TinHieuSOS sos) {
        String trangThaiStr = sos.getTrangThai() != null ? sos.getTrangThai().name() : "CHO_ADMIN";

        // SỬA TẠI ĐÂY: Đảm bảo tiến độ di chuyển của xe cứu hộ được bảo mật riêng tư
        messagingTemplate.convertAndSendToUser(
                sos.getUserId(),
                "/queue/sos-status",
                Map.of(
                        "idSOS", sos.getId(),
                        "trangThai", trangThaiStr,
                        "message", layThongDiepTrangThai(sos.getTrangThai())
                )
        );
    }

    private String layThongDiepTrangThai(TrangThaiXuLy trangThai) {
        if (trangThai == null) {
            return "Yêu cầu đã gửi đi!";
        }
        return switch (trangThai) {
            case CHO_ADMIN -> "Hệ thống đang tìm kiếm đội cứu hộ gần nhất...";
            case DA_TIEP_NHAN -> "Yêu cầu cứu hộ đã được tiếp nhận!";
            case DANG_DI_CHUYEN -> "Lực lượng cứu hộ đang trên đường di chuyển đến!";
            case DANG_XU_LY -> "Lực lượng cứu hộ đang tiến hành xử lý tại hiện trường!";
            case HOAN_THANH -> "Cứu hộ đã hoàn tất thành công!";
            case HUY_BO -> "Yêu cầu đã bị hủy!";
        };
    }
}