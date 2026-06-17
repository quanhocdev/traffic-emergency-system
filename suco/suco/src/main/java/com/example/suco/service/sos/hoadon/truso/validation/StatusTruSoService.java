package com.example.suco.service.sos.hoadon.truso.validation;

import com.example.suco.model.TinHieuSOS;
import org.springframework.stereotype.Service;

@Service
public class StatusTruSoService {
    
    public void validate(TinHieuSOS sos, Long trusoId) {

    if (sos.getIdTruSoTiepNhan() == null) {
        throw new RuntimeException("SOS chưa được tiếp nhận");
    }

    if (!sos.getIdTruSoTiepNhan().equals(trusoId)) {
        throw new RuntimeException("SOS không thuộc trụ sở");
    }

    if (sos.getTrangThai() == null || 
            !"DANG_XU_LY".equals(String.valueOf(sos.getTrangThai()).toUpperCase().trim())) {
            throw new RuntimeException("SOS chưa ở trạng thái xử lý");
        }
}

}
