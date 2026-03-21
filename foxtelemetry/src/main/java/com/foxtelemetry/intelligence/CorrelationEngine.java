package com.foxtelemetry.intelligence;

import androidx.annotation.NonNull;

import com.foxtelemetry.model.BaseEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class CorrelationEngine {
    private static final String GLOBAL_SESSION_KEY = "_global";

    private final int maxEventsPerSession;
    private final long correlationWindowMs;
    private final Map<String, Deque<BaseEvent>> eventsBySession = new HashMap<>();

    public CorrelationEngine(int maxEventsPerSession, long correlationWindowMs) {
        this.maxEventsPerSession = Math.max(20, maxEventsPerSession);
        this.correlationWindowMs = Math.max(1_000L, correlationWindowMs);
    }

    @NonNull
    public synchronized EventCorrelationWindow record(@NonNull BaseEvent event) {
        String sessionKey = resolveSessionKey(event);
        Deque<BaseEvent> buffer = eventsBySession.get(sessionKey);
        if (buffer == null) {
            buffer = new ArrayDeque<>();
            eventsBySession.put(sessionKey, buffer);
        }

        buffer.addLast(event);
        prune(buffer, event.timestamp);

        if (buffer.size() > maxEventsPerSession) {
            while (buffer.size() > maxEventsPerSession) {
                buffer.removeFirst();
            }
        }

        List<BaseEvent> snapshot = new ArrayList<>(buffer);
        long startMs = snapshot.isEmpty() ? event.timestamp : snapshot.get(0).timestamp;
        return new EventCorrelationWindow(sessionKey, startMs, event.timestamp, snapshot);
    }

    private void prune(@NonNull Deque<BaseEvent> buffer, long nowMs) {
        long minTimestamp = nowMs - correlationWindowMs;
        Iterator<BaseEvent> iterator = buffer.iterator();
        while (iterator.hasNext()) {
            BaseEvent event = iterator.next();
            if (event.timestamp >= minTimestamp) {
                break;
            }
            iterator.remove();
        }
    }

    @NonNull
    private String resolveSessionKey(@NonNull BaseEvent event) {
        if (event.sessionId != null && !event.sessionId.trim().isEmpty()) {
            return event.sessionId;
        }
        if (event.installId != null && !event.installId.trim().isEmpty()) {
            return event.installId;
        }
        return GLOBAL_SESSION_KEY;
    }
}
