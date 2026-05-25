package com.example.suco.service.sos.tinhieu.user;

import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.dto.sos.tinhieu.TinHieuSOSResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.service.sos.tinhieu.system.notification.TinHieuRealtimeService;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.WorkFlowService;

import java.util.Map;
@Service
public class TinHieuGuiService {

    @Autowired
    private TinHieuRealtimeService tinHieuRealtimeService;

    @Autowired
    private WorkFlowService processingService;

    public TinHieuSOS submitSOS(
            String uid,
            TinHieuSOSRequestDTO dto
    ) {

        Map<String, Object> ketQua =
                processingService.xuLyTinHieuSOS(uid, dto);

        TinHieuSOS sosDaLuu =
                (TinHieuSOS) ketQua.get("entity");

        tinHieuRealtimeService.realtimeGuiSOS(sosDaLuu);

        return sosDaLuu;
    }
}