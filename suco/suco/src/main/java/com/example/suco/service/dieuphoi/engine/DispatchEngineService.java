package com.example.suco.service.dieuphoi.engine;

import com.example.suco.model.SosDieuPhoi;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.model.enums.TrangThaiHoatDongTruSo;
import com.example.suco.repository.SosDieuPhoiRepository;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.dieuphoi.geohash.GeoHashService;
import com.example.suco.service.dieuphoi.distance.DistanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DispatchEngineService {

    @Autowired
    private GeoHashService geoHashService;

    @Autowired
    private DistanceService distanceService;

    @Autowired
    private SosDieuPhoiRepository dieuPhoiRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
private TinHieuSOSRepository tinHieuSOSRepository;

    // =====================================================
    // 1. START DISPATCH
    // =====================================================
  public void startDispatch(TinHieuSOS event) {

    double lat = event.getViDo();
    double lng = event.getKinhDo();

    List<TruSo> candidates =
            geoHashService.findTruSoInArea(lat, lng);

    TruSo best = candidates.stream()

            .filter(this::isAvailable)

            .min((a, b) -> Double.compare(

                    distanceService.distance(
                            lat, lng,
                            a.getViDo(), a.getKinhDo()
                    ),

                    distanceService.distance(
                            lat, lng,
                            b.getViDo(), b.getKinhDo()
                    )
            ))

            .orElse(null);

    if (best == null) {

        event.setTrangThai("CHO_ADMIN");

        tinHieuSOSRepository.save(event);

        messagingTemplate.convertAndSend(
                "/topic/admin",
                event
        );

        return;
    }

    event.setTrangThai("DANG_XU_LY");

    event.setIdTruSoTiepNhan(best.getId());

    event.setIdTruSoDeXuat(best.getId());

    tinHieuSOSRepository.save(event);

    SosDieuPhoi dp = new SosDieuPhoi();
dp.setSosId(event.getId());
dp.setTruSoId(best.getId());
dp.setTrangThai("CHO_TIEP_NHAN");
dp.setThuTu(0);

dieuPhoiRepo.save(dp);

    send(event, best.getId());
}

private boolean isAvailable(TruSo truSo) {

    return truSo.getTrangThaiHoatDong()
            == TrangThaiHoatDongTruSo.SAN_SANG;
}
    // =====================================================
    // SOCKET
    // =====================================================
    private void send(TinHieuSOS event, Long truSoId) {

        messagingTemplate.convertAndSend(
                "/topic/truso/" + truSoId,
                event
        );
    }
}