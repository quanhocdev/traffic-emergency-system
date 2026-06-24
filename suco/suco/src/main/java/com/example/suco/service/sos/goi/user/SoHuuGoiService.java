package com.example.suco.service.sos.goi.user;

import com.example.suco.dto.sos.goi.dangky.MuaGoiRequestDTO;
import com.example.suco.dto.sos.goi.dangky.MuaGoiUserResponseDTO;
import com.example.suco.mapper.goi.MuaGoiMapper;
import com.example.suco.model.Goi;
import com.example.suco.model.MuaGoi;
import com.example.suco.repository.sos.goi.CRUDGoiRepository;
import com.example.suco.repository.sos.goi.MuaGoiRepository;
import com.example.suco.service.sos.goi.user.validation.StatusGoiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SoHuuGoiService {

    @Autowired
    private CRUDGoiRepository goiRepository;

    @Autowired
    private MuaGoiRepository muaGoiRepository;

    @Autowired
    private StatusGoiService statusService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MuaGoiMapper muaGoiMapper;

    // Đăng ký gói mới cho người dùng
    public MuaGoi dangKyGoi(String userId, MuaGoiRequestDTO request) {

        List<MuaGoi> existing = muaGoiRepository.findByUserId(userId);

        for (MuaGoi mg : existing) {
            statusService.validateCanBuy(mg);
        }

        Goi goi = goiRepository.findById(request.getGoiId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói"));

        // 2. Sửa thành gọi qua instance cụ thể
        MuaGoi entity = muaGoiMapper.toEntity(request, userId, goi);

        MuaGoi saved = muaGoiRepository.save(entity);

        messagingTemplate.convertAndSendToUser(
        userId,                   // Đích danh uid của user nhận (Được lấy từ Firebase Token lúc connect)
        "/queue/package-status",  // Điểm đến cụ thể (Spring tự động map thành /user/queue/package-status)
        "Bạn đã đăng ký gói thành công"
);

        return saved;
    }

    // Lấy danh sách gói đã mua của người dùng
    public List<MuaGoiUserResponseDTO> getGoiByUserId(String userId) {

        List<MuaGoi> list = muaGoiRepository.findByUserId(userId);

        return list.stream().map(mg -> {

            Goi goi = goiRepository.findById(mg.getGoiId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy gói"));

            // 2. Sửa thành gọi qua instance cụ thể
            return muaGoiMapper.toResponse(mg, goi);

        }).collect(Collectors.toList());
    }

    // Hủy gói đã mua (Chỉ hủy khi chưa ACTIVE) 
    public void huyGoi(Long id, String userId) {

        MuaGoi mg = muaGoiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói"));

        if (!mg.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền");
        }

        statusService.validateCanCancel(mg);

        mg.setTrangThai("CANCELLED");
        muaGoiRepository.save(mg);

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/package-status",
                "REFRESH"
        );
    }
}