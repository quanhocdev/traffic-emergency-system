package com.example.suco.service.sos.hoadon.quanly.validation;

import com.example.suco.model.TinHieuSOS;
import org.springframework.stereotype.Service;

@Service
public class StatusService {
    
    public void validate(TinHieuSOS sos, Long trusoId) {

    if (sos.getIdTruSoTiepNhan() == null) {
        throw new RuntimeException("SOS chưa được tiếp nhận");
    }

    if (!sos.getIdTruSoTiepNhan().equals(trusoId)) {
        throw new RuntimeException("SOS không thuộc trụ sở");
    }

    if (!"DANG_XU_LY".equals(sos.getTrangThai())) {
        throw new RuntimeException("SOS chưa ở trạng thái xử lý");
    }
}

}
