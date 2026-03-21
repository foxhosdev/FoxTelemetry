package com.foxtelemetry.intelligence.rules;

import androidx.annotation.NonNull;

import com.foxtelemetry.intelligence.EventCorrelationWindow;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.AnomalyCategory;
import com.foxtelemetry.model.BaseEvent;
import com.foxtelemetry.model.Severity;
import com.foxtelemetry.model.TrackEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CallbackStormRule extends BaseRule {
    private final int threshold;
    private final long windowMs;

    public CallbackStormRule(int threshold, long windowMs, long cooldownMs) {
        super(cooldownMs);
        this.threshold = Math.max(3, threshold);
        this.windowMs = Math.max(1_000L, windowMs);
    }

    @NonNull
    @Override
    public String getId() {
        return "callback_storm";
    }

    @NonNull
    @Override
    public List<Anomaly> evaluate(@NonNull BaseEvent event, @NonNull EventCorrelationWindow window) {
        if (!(event instanceof TrackEvent)) {
            return java.util.Collections.emptyList();
        }

        TrackEvent trackEvent = (TrackEvent) event;
        long minTimestamp = event.timestamp - windowMs;
        int count = 0;
        for (BaseEvent candidate : window.getEvents()) {
            if (!(candidate instanceof TrackEvent) || candidate.timestamp < minTimestamp) {
                continue;
            }
            TrackEvent trackCandidate = (TrackEvent) candidate;
            if (trackEvent.name.equals(trackCandidate.name)) {
                count++;
            }
        }

        if (count < threshold) {
            return java.util.Collections.emptyList();
        }

        String emissionKey = getId() + "|" + window.getSessionKey() + "|" + trackEvent.name;
        if (!shouldEmit(emissionKey, event.timestamp)) {
            return java.util.Collections.emptyList();
        }

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("eventName", trackEvent.name);
        context.put("count", count);
        context.put("windowMs", windowMs);

        Anomaly anomaly = new Anomaly(
                "CALLBACK_STORM",
                "Callback storm detected",
                Severity.WARNING,
                AnomalyCategory.LIFECYCLE,
                "The same callback-like signal was emitted too frequently in a short time window"
        );
        anomaly.context.putAll(context);
        return java.util.Collections.singletonList(anomaly);
    }
}
