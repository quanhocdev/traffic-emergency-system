package com.example.suco.service.sos.tinhieu.user;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.enums.TrangThaiXuLy; 
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.sos.tinhieu.notification.TinHieuRealtimeService;

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

        if (sos.getTrangThai() == null || !sos.getTrangThai().canBeCancelledByUser()) {
            throw new RuntimeException("Không thể hủy vì yêu cầu đang xử lý thực tế hoặc đã kết thúc.");
        }

        sos.setTrangThai(TrangThaiXuLy.HUY_BO);
        tinHieuSOSRepository.save(sos);

        // 2. Realtime notify
        tinHieuRealtimeService.realtimeHuySOS(sos);
    }
}