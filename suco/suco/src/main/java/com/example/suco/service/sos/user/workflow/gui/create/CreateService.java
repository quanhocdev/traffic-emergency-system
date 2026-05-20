package com.example.suco.service.sos.user.workflow.gui.create;

import org.springframework.stereotype.Service;

import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.model.TinHieuSOS;

@Service
public class CreateService {
    public TinHieuSOS createSOS(String uid, TinHieuSOSRequestDTO dto) {
    TinHieuSOS sos = new TinHieuSOS();
    sos.setUserId(uid);
    sos.setViDo(dto.getViDo());
    sos.setKinhDo(dto.getKinhDo());
    sos.setGhiChu(dto.getGhiChu());
    sos.setTrangThai("CHO_XU_LY");
    return sos;
}
}
