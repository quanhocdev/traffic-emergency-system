package com.example.suco.service.dieuphoi.engine;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DispatchStateService {

    // ================================
    // STATE MODEL
    // ================================
    public static class State {
        public Long sosId;

        public List<Long> queue;          // danh sách trụ sở (gần → xa)
        public int index;                // vị trí hiện tại

        public LocalDateTime lastUpdate; // thời gian gửi gần nhất
        public String status;            // DANG_CHO / DA_TIEP_NHAN / HET_TRU_SO / HUY_BO
    }

    private final Map<Long, State> store = new ConcurrentHashMap<>();

    // ================================
    // CREATE STATE
    // ================================
    public State create(Long sosId, List<Long> queue) {
        State s = new State();
        s.sosId = sosId;
        s.queue = queue;
        s.index = 0;
        s.lastUpdate = LocalDateTime.now();
        s.status = "DANG_CHO";

        store.put(sosId, s);
        return s;
    }

    // ================================
    // GET STATE
    // ================================
    public State get(Long sosId) {
        return store.get(sosId);
    }

    // ================================
    // CURRENT TRU SO
    // ================================
    public Long getCurrent(Long sosId) {
        State s = store.get(sosId);
        if (s == null || s.queue == null) return null;

        if (s.index >= s.queue.size()) return null;

        return s.queue.get(s.index);
    }

    // ================================
    // NEXT STEP
    // ================================
    public Long moveNext(Long sosId) {
        State s = store.get(sosId);
        if (s == null) return null;

        s.index++;
        s.lastUpdate = LocalDateTime.now();

        if (s.queue == null || s.index >= s.queue.size()) {
            s.status = "HET_TRU_SO";
            store.remove(sosId);
            return null;
        }

        return s.queue.get(s.index);
    }

    // ================================
    // ACCEPT (TIẾP NHẬN)
    // ================================
    public void accept(Long sosId) {
        State s = store.get(sosId);
        if (s == null) return;

        s.status = "DA_TIEP_NHAN";
        store.remove(sosId);
    }

    // ================================
    // CANCEL / HỦY
    // ================================
    public void cancel(Long sosId) {
        State s = store.get(sosId);
        if (s == null) return;

        s.status = "HUY_BO";
        store.remove(sosId);
    }

    // ================================
    // TIMEOUT CHECK (cho scheduler)
    // ================================
    public List<State> getExpired(long timeoutSeconds) {
        LocalDateTime limit = LocalDateTime.now().minusSeconds(timeoutSeconds);

        return store.values().stream()
                .filter(s -> "DANG_CHO".equals(s.status))
                .filter(s -> s.lastUpdate.isBefore(limit))
                .toList();
    }

    // ================================
    // ALL STATES (debug)
    // ================================
    public Collection<State> getAll() {
        return store.values();
    }
}