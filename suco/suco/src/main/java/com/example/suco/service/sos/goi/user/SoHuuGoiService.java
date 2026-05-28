package com.example.suco.service.sos.goi.user;

import com.example.suco.dto.sos.goi.dangky.MuaGoiRequestDTO;
import com.example.suco.dto.sos.goi.dangky.MuaGoiResponseDTO;
import com.example.suco.mapper.MuaGoiMapper;
import com.example.suco.model.Goi;
import com.example.suco.model.MuaGoi;
import com.example.suco.repository.sos.goi.CRUDGoiRepository;
import com.example.suco.repository.sos.goi.MuaGoiRepository;
import com.example.suco.service.sos.goi.user.validation.StatusService;

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
    private StatusService statusService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    // Đăng ký gói mới cho người dùng
   public MuaGoi dangKyGoi(String userId, MuaGoiRequestDTO request) {

    List<MuaGoi> existing = muaGoiRepository.findByUserId(userId);

    for (MuaGoi mg : existing) {
        statusService.validateCanBuy(mg);
    }

    Goi goi = goiRepository.findById(request.getGoiId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy gói"));

    MuaGoi entity = MuaGoiMapper.toEntity(request, userId, goi);

    MuaGoi saved = muaGoiRepository.save(entity);

    messagingTemplate.convertAndSend(
            "/topic/package-status/" + userId,
            "Bạn đã đăng ký gói thành công"
    );

    return saved;
}
    // Lấy danh sách gói đã mua của người dùng
    public List<MuaGoiResponseDTO> getGoiByUserId(String userId) {

    List<MuaGoi> list = muaGoiRepository.findByUserId(userId);

    return list.stream().map(mg -> {

        String tenGoi = goiRepository.findById(mg.getGoiId())
                .map(g -> g.getTen())
                .orElse("Không xác định");

        return MuaGoiMapper.toResponse(mg, tenGoi);

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

    messagingTemplate.convertAndSend(
            "/topic/package-status/" + userId,
            "REFRESH"
    );
}
}