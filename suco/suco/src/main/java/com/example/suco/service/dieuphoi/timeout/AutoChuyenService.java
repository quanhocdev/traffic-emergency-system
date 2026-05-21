// package com.example.suco.service.dieuphoi.timeout;

// import com.example.suco.model.TinHieuSOS;
// import com.example.suco.service.dieuphoi.engine.DispatchEngineService;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// @Component
// public class AutoChuyenService {

//     private static final Logger logger = LoggerFactory.getLogger(AutoChuyenService.class);

//     // timeout 60s giống engine
//     private static final int TIMEOUT_SECONDS = 60;

//     @Autowired
//     private DispatchEngineService dispatchEngineService;

//     /**
//      * Chạy định kỳ kiểm tra các event quá hạn
//      */
//     @Scheduled(fixedRate = 10000) // 10 giây
//     public void checkAndAutoMove() {

//         try {
//             Map<Long, RetryService.SessionState> all = retryService.getAll();

//             if (all.isEmpty()) return;

//             List<Long> expiredEvents = new ArrayList<>();

//             LocalDateTime now = LocalDateTime.now();

//             // 1. tìm session quá hạn
//             for (RetryService.SessionState session : all.values()) {

//                 if (session == null || session.queue == null) continue;

//                 if ("WAITING".equals(session.status)
//                         && session.lastUpdate != null
//                         && session.lastUpdate.plusSeconds(TIMEOUT_SECONDS).isBefore(now)) {

//                     expiredEvents.add(session.eventId);
//                 }
//             }

//             if (expiredEvents.isEmpty()) return;

//             logger.info("[AUTO DISPATCH] Found {} expired events", expiredEvents.size());

//             // 2. xử lý từng event
//             for (Long eventId : expiredEvents) {

//                 RetryService.SessionState session = retryService.get(eventId);
//                 if (session == null) continue;

//                 try {
//                     // load event giả (bạn có thể thay bằng service DB sau)
//                     TinHieuSOS fakeEvent = new TinHieuSOS();
//                     fakeEvent.setId(eventId);

//                     dispatchEngineService.moveNext(fakeEvent);

//                 } catch (Exception e) {
//                     logger.error("[AUTO DISPATCH] Error event {}: {}", eventId, e.getMessage());
//                 }
//             }

//         } catch (Exception e) {
//             logger.error("[AUTO DISPATCH] Scheduler error: {}", e.getMessage());
//         }
//     }
// }