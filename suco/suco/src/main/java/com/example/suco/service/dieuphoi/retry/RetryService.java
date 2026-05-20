package com.example.suco.service.dieuphoi.retry;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RetryService {

    public static class SessionState {
        public Long eventId;
        public int index;
        public LocalDateTime lastUpdate;
        public String status; // WAITING / DONE / FAIL
    }

    private final Map<Long, SessionState> memory = new ConcurrentHashMap<>();

    public SessionState create(Long eventId) {
        SessionState s = new SessionState();
        s.eventId = eventId;
        s.index = 0;
        s.lastUpdate = LocalDateTime.now();
        s.status = "WAITING";
        memory.put(eventId, s);
        return s;
    }

    public SessionState get(Long eventId) {
        return memory.get(eventId);
    }

    public void next(Long eventId) {
        SessionState s = memory.get(eventId);
        if (s != null) {
            s.index++;
            s.lastUpdate = LocalDateTime.now();
        }
    }

    public void done(Long eventId) {
        memory.remove(eventId);
    }

    public Map<Long, SessionState> getAll() {
        return memory;
    }
}