package com.example.suco.service.sos.user.workflow.gui.vip;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.MuaGoiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VipService {

    @Autowired
    private MuaGoiRepository muaGoiRepository;

    public boolean checkVip(String uid) {
        return muaGoiRepository.findByUserId(uid)
                .stream()
                .anyMatch(mg -> "ACTIVE".equalsIgnoreCase(mg.getTrangThai()));
    }

    public void handleVipFlow(boolean laVip, TinHieuSOS sos, TruSo truSo, String uid) {

        if (!laVip || truSo == null) return;

        sos.setIdTruSoTiepNhan(truSo.getId());
        sos.setIdTruSoDeXuat(truSo.getId());
        sos.setTrangThai("DANG_XU_LY");

        // VIP bypass: không cần queue / retry nữa
    }
}