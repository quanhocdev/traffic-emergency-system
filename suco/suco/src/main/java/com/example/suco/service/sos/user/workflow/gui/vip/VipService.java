package com.example.suco.service.sos.user.workflow.gui.vip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.MuaGoiRepository;
import com.example.suco.service.DieuPhoiSOSService;

@Service
public class VipService {
   
    @Autowired
    private MuaGoiRepository muaGoiRepository;


    @Autowired
    private DieuPhoiSOSService dieuPhoiService;

    public boolean checkVip(String uid) {
    return muaGoiRepository.findByUserId(uid)
            .stream()
            .anyMatch(mg -> "ACTIVE".equalsIgnoreCase(mg.getTrangThai()));
    }
    public void handleVipFlow(boolean laVip, TinHieuSOS sos, TruSo truSo, String uid) {
    if (!laVip || truSo == null) return;

    sos.setIdTruSoTiepNhan(truSo.getId());
    sos.setTrangThai("DANG_XU_LY");

    dieuPhoiService.danhDauDaTiepNhan(
            sos.getId(),
            truSo.getId()
    );
}
    
}
