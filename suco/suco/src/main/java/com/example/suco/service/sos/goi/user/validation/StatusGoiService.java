package com.example.suco.service.sos.goi.user.validation;

import com.example.suco.model.MuaGoi;
import org.springframework.stereotype.Service;

@Service
public class StatusGoiService {

    public void validateCanBuy(MuaGoi mg) {

        if ("PENDING".equals(mg.getTrangThai())) {
            throw new RuntimeException("Bạn phải hủy gói đang chờ để mua gói mới");
        }

        if ("ACTIVE".equals(mg.getTrangThai())) {
            throw new RuntimeException("Bạn đã có gói đang hoạt động");
        }
    }

    public void validateCanCancel(MuaGoi mg) {

        if ("ACTIVE".equals(mg.getTrangThai())) {
            throw new RuntimeException("Gói đang hoạt động, không thể hủy!");
        }
    }
}