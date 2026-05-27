package com.example.suco.service.sos.payment.hoadon.truso.validation;
import com.example.suco.model.Goi;
import com.example.suco.repository.sos.goi.CRUDGoiRepository;
import com.example.suco.repository.sos.goi.SoHuuGoiRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class VipSOSService {

    @Autowired
    private SoHuuGoiRepository muaGoiRepository;

    @Autowired
    private CRUDGoiRepository goiRepository;

    public Optional<Goi> getActiveGoi(String userId) {

        return muaGoiRepository.findFirstByUserIdAndTrangThai(userId, "ACTIVE")
                .map(m -> goiRepository.findById(m.getGoiId()).orElse(null));
    }
}
