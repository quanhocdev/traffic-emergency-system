package com.example.suco.service.dieuphoi.engine;

import com.example.suco.model.TruSo;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.service.dieuphoi.distance.DistanceService;
import com.example.suco.service.dieuphoi.geohash.GeoHashService;
import com.example.suco.service.dieuphoi.queue.DispatchQueueService;
import com.example.suco.service.dieuphoi.retry.RetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispatchEngineService {

    @Autowired
    private GeoHashService geoHashService;

    @Autowired
    private DistanceService distanceService;

    @Autowired
    private DispatchQueueService queueService;

    @Autowired
    private RetryService retryService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ================================
    // 1. START DISPATCH (SOS / INCIDENT)
    // ================================
    public void startDispatch(TinHieuSOS event) {

    double lat = event.getViDo();
    double lng = event.getKinhDo();

    List<TruSo> candidates = geoHashService.findTruSoInArea(lat, lng);

    if (candidates.isEmpty()) return;

    List<Long> queue = queueService.buildQueue(candidates, lat, lng);

    // 👉 RetryService giữ toàn bộ queue
    retryService.create(event.getId(), queue);

    // 👉 lấy từ retryService luôn (KHÔNG dùng queue nữa)
    Long first = retryService.getCurrent(event.getId());

        event.setIdTruSoDeXuat(first);


    sendToTruSo(event, first);
}
    // ================================
    // 2. NEXT STEP (TIMEOUT / REJECT)
    // ================================
    public void moveNext(TinHieuSOS event) {

    retryService.moveNext(event.getId());

    Long next = retryService.getCurrent(event.getId());

    if (next == null) {
        retryService.done(event.getId());
        return;
    }

        event.setIdTruSoDeXuat(next);


    sendToTruSo(event, next);
}

    // ================================
    // 3. SEND WEB SOCKET
    // ================================
    private void sendToTruSo(TinHieuSOS event, Long truSoId) {

        messagingTemplate.convertAndSend(
                "/topic/truso/" + truSoId,
                event
        );
    }

    public void cancel(Long sosId) {
    retryService.done(sosId);
}
}