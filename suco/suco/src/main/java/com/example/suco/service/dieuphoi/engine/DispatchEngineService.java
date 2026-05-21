package com.example.suco.service.dieuphoi.engine;

import com.example.suco.model.SosDieuPhoi;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.SosDieuPhoiRepository;
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

    // =====================================================
    // 1. START DISPATCH
    // =====================================================
    public void startDispatch(TinHieuSOS event) {

        List<TruSo> candidates = geoHashService.findTruSoInArea(
                event.getViDo(),
                event.getKinhDo()
        );

        if (candidates == null || candidates.isEmpty()) return;

        List<Long> queue = queueService.buildQueue(
                candidates,
                event.getViDo(),
                event.getKinhDo()
        );

        if (queue.isEmpty()) return;

        Long first = queue.get(0);

        // INSERT DB
        SosDieuPhoi dp = new SosDieuPhoi();
        dp.setSosId(event.getId());
        dp.setTruSoId(first);
        dp.setThuTu(0);
        dp.setTrangThai("CHO_TIEP_NHAN");
        dp.setThoiGianGui(LocalDateTime.now());

        dieuPhoiRepo.save(dp);

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

        if (current == null) return;

        current.setTrangThai("TU_CHOI");
        current.setThoiGianXuLy(LocalDateTime.now());

        dieuPhoiRepo.save(current);

        int nextIndex = current.getThuTu() + 1;

        List<SosDieuPhoi> all = dieuPhoiRepo.findBySosIdOrderByThuTuAsc(event.getId());

        if (nextIndex >= all.size()) return;

        SosDieuPhoi next = all.get(nextIndex);

        SosDieuPhoi newRow = new SosDieuPhoi();
        newRow.setSosId(event.getId());
        newRow.setTruSoId(next.getTruSoId());
        newRow.setThuTu(nextIndex);
        newRow.setTrangThai("CHO_TIEP_NHAN");
        newRow.setThoiGianGui(LocalDateTime.now());

        dieuPhoiRepo.save(newRow);

        event.setIdTruSoDeXuat(next.getTruSoId());

        send(event, next.getTruSoId());
    }

    // =====================================================
    // 3. TIMEOUT
    // =====================================================
    public void timeout(TinHieuSOS event) {
        reject(event);
    }

    // =====================================================
    // 4. ACCEPT
    // =====================================================
    public void accept(TinHieuSOS event, Long truSoId) {

        SosDieuPhoi dp = dieuPhoiRepo
                .findBySosIdAndTruSoId(event.getId(), truSoId)
                .orElse(null);

        if (dp != null) {
            dp.setTrangThai("TIEP_NHAN");
            dp.setThoiGianXuLy(LocalDateTime.now());
            dieuPhoiRepo.save(dp);
        }

        event.setIdTruSoTiepNhan(truSoId);
        event.setIdTruSoDeXuat(truSoId);
        event.setTrangThai("DANG_XU_LY");

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