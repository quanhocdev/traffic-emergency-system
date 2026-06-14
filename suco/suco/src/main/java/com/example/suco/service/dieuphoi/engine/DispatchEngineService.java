package com.example.suco.service.dieuphoi.engine;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.model.enums.TrangThaiHoatDongTruSo;
import com.example.suco.model.enums.TrangThaiXuLy; 
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.dieuphoi.distance.DieuPhoiDistanceService;
import com.example.suco.service.dieuphoi.geohash.GeoHashService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispatchEngineService {

    @Autowired
    private GeoHashService geoHashService;

    @Autowired
    private DieuPhoiDistanceService distanceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    public void startDispatch(TinHieuSOS event) {

        double lat = event.getViDo();
        double lng = event.getKinhDo();

        List<TruSo> candidates =
                geoHashService.findTruSoInArea(lat, lng);

        TruSo best = candidates.stream()
                .filter(this::isAvailable)
                .min((a, b) -> Double.compare(
                        distanceService.distance(lat, lng, a.getViDo(), a.getKinhDo()),
                        distanceService.distance(lat, lng, b.getViDo(), b.getKinhDo())
                ))
                .orElse(null);

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

    private boolean isAvailable(TruSo truSo) {
        return truSo.getTrangThaiHoatDong()
                == TrangThaiHoatDongTruSo.SAN_SANG;
    }

    private void send(TinHieuSOS event, Long truSoId) {
        messagingTemplate.convertAndSend(
                "/topic/truso/" + truSoId,
                event
        );
    }
}