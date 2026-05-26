package com.example.suco.service.sos.tinhieu.user;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.sos.tinhieu.system.notification.TinHieuRealtimeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HuyTinHieuService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private TinHieuRealtimeService tinHieuRealtimeService;

    public void cancelSOS(Long id, String currentUid) {

        TinHieuSOS sos = tinHieuSOSRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));

        if (!sos.getUserId().equals(currentUid)) {
            throw new RuntimeException("Bạn không có quyền hủy yêu cầu này.");
        }

        if (!"CHO_XU_LY".equals(sos.getTrangThai())) {
            throw new RuntimeException("Không thể hủy vì yêu cầu đang xử lý hoặc đã kết thúc.");
        }

        // 1. update DB
        sos.setTrangThai("HUY_BO");
        tinHieuSOSRepository.save(sos);

        // 2. realtime notify
        tinHieuRealtimeService.realtimeHuySOS(sos);
    }
}