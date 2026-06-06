package com.example.suco.controller.sos.tinhieu.system;
import com.example.suco.dto.sos.tinhieu.SOSMapResponseDTO;
import com.example.suco.service.sos.tinhieu.notification.TinHieuRealtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sos")
public class SosMapController {

    @Autowired
    private TinHieuRealtimeService service;

    @GetMapping("/map")
    public List<SOSMapResponseDTO> getMap() {
        return service.getMapData();
    }
}