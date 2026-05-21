package com.example.suco.service.dieuphoi.engine;

import com.example.suco.model.SosDieuPhoi;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.SosDieuPhoiRepository;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.dieuphoi.geohash.GeoHashService;
import com.example.suco.service.dieuphoi.queue.DispatchQueueService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DispatchEngineService {

    @Autowired
    private GeoHashService geoHashService;

    @Autowired
    private DispatchQueueService queueService;

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

        List<TruSo> candidates = geoHashService.findTruSoInArea(
                event.getViDo(),
                event.getKinhDo()
        );

        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        List<Long> queue = queueService.buildQueue(
                candidates,
                event.getViDo(),
                event.getKinhDo()
        );

        if (queue == null || queue.isEmpty()) {
            return;
        }

        // SAVE FULL QUEUE
        for (int i = 0; i < queue.size(); i++) {

            SosDieuPhoi dp = new SosDieuPhoi();

            dp.setSosId(event.getId());
            dp.setTruSoId(queue.get(i));
            dp.setThuTu(i);

            // Trụ sở đầu tiên
            if (i == 0) {

                dp.setTrangThai("CHO_TIEP_NHAN");
                dp.setThoiGianGui(LocalDateTime.now());

            } else {

                // Các trụ sở phía sau
                dp.setTrangThai("HANG_CHO");
            }

            dieuPhoiRepo.save(dp);
        }

        Long first = queue.get(0);

        event.setIdTruSoDeXuat(first);

        send(event, first);
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

        moveToNext(event, current.getThuTu());
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

        moveToNext(event, current.getThuTu());
    }

    // =====================================================
    // 4. ACCEPT
    // =====================================================

public void accept(TinHieuSOS event, Long truSoId) {

    SosDieuPhoi current = dieuPhoiRepo
            .findBySosIdAndTruSoIdAndTrangThai(
                    event.getId(),
                    truSoId,
                    "CHO_TIEP_NHAN"
            )
            .orElse(null);

    if (current == null) {
        return;
    }

    // CURRENT -> TIEP_NHAN
    current.setTrangThai("TIEP_NHAN");
    current.setThoiGianXuLy(LocalDateTime.now());

    dieuPhoiRepo.save(current);

    // DELETE HANG_CHO
    List<SosDieuPhoi> all =
            dieuPhoiRepo.findBySosIdOrderByThuTuAsc(event.getId());

    for (SosDieuPhoi item : all) {

        if ("HANG_CHO".equals(item.getTrangThai())) {

            dieuPhoiRepo.delete(item);
        }
    }

    // UPDATE SOS
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
    // MOVE NEXT QUEUE
    // =====================================================
    private void moveToNext(TinHieuSOS event, int currentIndex) {

        int nextIndex = currentIndex + 1;

        List<SosDieuPhoi> all =
                dieuPhoiRepo.findBySosIdOrderByThuTuAsc(event.getId());

        if (nextIndex >= all.size()) {

            event.setTrangThai("KHONG_CO_TRU_SO");

            return;
        }

        SosDieuPhoi next = all.get(nextIndex);

        // NEXT -> ACTIVE
        next.setTrangThai("CHO_TIEP_NHAN");
        next.setThoiGianGui(LocalDateTime.now());

        dieuPhoiRepo.save(next);

        event.setIdTruSoDeXuat(next.getTruSoId());

        send(event, next.getTruSoId());
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