package com.foxtelemetry.intelligence;

import androidx.annotation.NonNull;

import com.foxtelemetry.model.AnomalyEvent;
import com.foxtelemetry.model.BaseEvent;
import com.foxtelemetry.model.ErrorEvent;
import com.foxtelemetry.model.TraceEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EventCorrelationWindow {
    private final String sessionKey;
    private final long windowStartMs;
    private final long windowEndMs;
    private final List<BaseEvent> events;

    EventCorrelationWindow(@NonNull String sessionKey,
                           long windowStartMs,
                           long windowEndMs,
                           @NonNull List<BaseEvent> events) {
        this.sessionKey = sessionKey;
        this.windowStartMs = windowStartMs;
        this.windowEndMs = windowEndMs;
        this.events = Collections.unmodifiableList(new ArrayList<>(events));
    }

    @NonNull
    public String getSessionKey() {
        return sessionKey;
    }

    public long getWindowStartMs() {
        return windowStartMs;
    }

    public long getWindowEndMs() {
        return windowEndMs;
    }

    @NonNull
    public List<BaseEvent> getEvents() {
        return events;
    }

    @NonNull
    public List<ErrorEvent> getErrorEvents() {
        List<ErrorEvent> out = new ArrayList<>();
        for (BaseEvent event : events) {
            if (event instanceof ErrorEvent) {
                out.add((ErrorEvent) event);
            }
        }
        return out;
    }

    @NonNull
    public List<TraceEvent> getTraceEvents() {
        List<TraceEvent> out = new ArrayList<>();
        for (BaseEvent event : events) {
            if (event instanceof TraceEvent) {
                out.add((TraceEvent) event);
            }
        }
        return out;
    }

    @NonNull
    public List<AnomalyEvent> getAnomalies() {
        List<AnomalyEvent> out = new ArrayList<>();
        for (BaseEvent event : events) {
            if (event instanceof AnomalyEvent) {
                out.add((AnomalyEvent) event);
            }
        }
        return out;
    }
}
