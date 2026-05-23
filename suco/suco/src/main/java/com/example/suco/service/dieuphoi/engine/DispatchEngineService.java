package com.example.suco.service.dieuphoi.engine;

import com.example.suco.model.SosDieuPhoi;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
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

    // 1. lấy candidates bằng geohash (không queue nữa)
    List<TruSo> candidates = geoHashService.findTruSoInArea(lat, lng);

    if (candidates == null || candidates.isEmpty()) {
        return;
    }

    // 2. chia nhóm theo khoảng cách
    List<TruSo> fast = new ArrayList<>();   // <= 5km
    List<TruSo> mid = new ArrayList<>();    // 5–15km

    for (TruSo ts : candidates) {

        double d = distanceService.distance(
                lat, lng,
                ts.getViDo(), ts.getKinhDo()
        );

        if (d <= 5000) {
            fast.add(ts);
        } 
        else if (d <= 15000) {
            mid.add(ts);
        }
    }

    // =========================
    // CASE 1: FAST PATH (<=5km)    
    // =========================
    if (!fast.isEmpty()) {

        TruSo best = fast.get(0); // hoặc min distance nếu muốn

        event.setTrangThai("DANG_XU_LY");
        event.setIdTruSoTiepNhan(best.getId());
        event.setIdTruSoDeXuat(best.getId());

        tinHieuSOSRepository.save(event);

        send(event, best.getId());
        return;
    }

    // =========================
    // CASE 2: MID (5–15km)
    // =========================
    if (!mid.isEmpty()) {

        event.setTrangThai("CHO_XU_LY");
        tinHieuSOSRepository.save(event);

        for (TruSo ts : mid) {

    SosDieuPhoi dp = new SosDieuPhoi();
    dp.setSosId(event.getId());
    dp.setTruSoId(ts.getId());
    dp.setTrangThai("CHO_TIEP_NHAN");

    dieuPhoiRepo.save(dp);

    send(event, ts.getId());
}

        return;
    }

    // =========================
    // CASE 3: fallback (nếu cần)
    // =========================
    event.setTrangThai("CHO_ADMIN");
    tinHieuSOSRepository.save(event);

    messagingTemplate.convertAndSend("/topic/admin", event);
}

    // =====================================================
    // 2. REJECT
    // =====================================================
    public void reject(TinHieuSOS event) {

        SosDieuPhoi current = dieuPhoiRepo
                .findBySosIdAndTrangThai(event.getId(), "CHO_TIEP_NHAN")
                .orElse(null);

        if (current == null) {
            return;
        }

        // CURRENT -> TU_CHOI
        current.setTrangThai("TU_CHOI");
        current.setThoiGianXuLy(LocalDateTime.now());

        dieuPhoiRepo.save(current);
    }

    // =====================================================
    // 3. TIMEOUT
    // =====================================================
    public void timeout(TinHieuSOS event) {

        SosDieuPhoi current = dieuPhoiRepo
                .findBySosIdAndTrangThai(event.getId(), "CHO_TIEP_NHAN")
                .orElse(null);

        if (current == null) {
            return;
        }

        // CURRENT -> TIMEOUT
        current.setTrangThai("TIMEOUT");
        current.setThoiGianXuLy(LocalDateTime.now());

        dieuPhoiRepo.save(current);
    }

    // =====================================================
    // 4. ACCEPT
    // =====================================================

 public void accept(TinHieuSOS event, Long truSoId) {

        // check đã có ai nhận chưa (RACE CONDITION PROTECTION)
        if (event.getIdTruSoTiepNhan() != null) {
            return;
        }

        event.setTrangThai("DANG_XU_LY");
        event.setIdTruSoTiepNhan(truSoId);
        event.setIdTruSoDeXuat(truSoId);

        tinHieuSOSRepository.save(event);

        send(event, truSoId);
    }

    // =====================================================
    // 5. CANCEL
    // =====================================================
    public void cancel(TinHieuSOS event) {

        List<SosDieuPhoi> list =
                dieuPhoiRepo.findBySosIdOrderByThuTuAsc(event.getId());

        for (SosDieuPhoi dp : list) {

            if (!"TIEP_NHAN".equals(dp.getTrangThai())) {

                dp.setTrangThai("HUY_BO");
                dp.setThoiGianXuLy(LocalDateTime.now());
            }
        }

        dieuPhoiRepo.saveAll(list);

        event.setTrangThai("HUY_BO");
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