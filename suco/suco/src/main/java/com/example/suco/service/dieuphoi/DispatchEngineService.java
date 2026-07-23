package com.example.suco.service.dieuphoi;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.model.enums.TrangThaiHoatDongTruSo;
import com.example.suco.model.enums.TrangThaiXuLy; 
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.distance.DistanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispatchEngineService {


    @Autowired
    private TruSoSelectorService truSoSelectorService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    public void startDispatch(TinHieuSOS event) {

        double lat = event.getViDo();
        double lng = event.getKinhDo();

        TruSo best = truSoSelectorService.selectNearest(lat, lng);

        if (best == null) {
            event.setTrangThai(TrangThaiXuLy.CHO_ADMIN);

            tinHieuSOSRepository.save(event);

            messagingTemplate.convertAndSend("/topic/admin", event);
            return;
        }

        event.setTrangThai(TrangThaiXuLy.DA_TIEP_NHAN);

        event.setIdTruSoTiepNhan(best.getId());

        tinHieuSOSRepository.save(event);

        send(event, best.getId());
    }

    private void send(TinHieuSOS event, Long truSoId) {
        messagingTemplate.convertAndSend(
                "/topic/truso/" + truSoId,
                event
        );
    }
}