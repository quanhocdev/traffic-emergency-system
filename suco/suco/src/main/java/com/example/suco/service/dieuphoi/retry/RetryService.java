package com.example.suco.service.dieuphoi.retry;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

@Service
public class RetryService {

    public static class SessionState {

        public Long eventId;
        public List<Long> queue;
        public int index;
        public LocalDateTime lastUpdate;
        public String status; // WAITING / DONE / FAIL
    }

    private final Map<Long, SessionState> memory = new ConcurrentHashMap<>();

    public SessionState create(Long eventId, List<Long> queue) {
        SessionState s = new SessionState();
        s.eventId = eventId;
        s.queue = queue;
        s.index = 0;
        s.lastUpdate = LocalDateTime.now();
        s.status = "WAITING";
        memory.put(eventId, s);
        return s;
    }

    public SessionState get(Long eventId) {
        return memory.get(eventId);
    }

    public void moveNext(Long eventId) {
    SessionState s = memory.get(eventId);
    if (s == null) return;

    s.index++;
    s.lastUpdate = LocalDateTime.now();

    if (s.queue != null && s.index >= s.queue.size()) {
        s.status = "FAIL";
        memory.remove(eventId);
    }
}
public Long getCurrent(Long eventId) {
    SessionState s = memory.get(eventId);
    if (s == null) return null;

    if (s.queue == null || s.index >= s.queue.size()) return null;

    return s.queue.get(s.index);
}

    public void done(Long eventId) {
        memory.remove(eventId);
    }

    public Map<Long, SessionState> getAll() {
        return memory;
    }
}