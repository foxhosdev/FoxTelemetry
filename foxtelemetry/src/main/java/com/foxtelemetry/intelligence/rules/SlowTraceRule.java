package com.foxtelemetry.intelligence.rules;

import androidx.annotation.NonNull;

import com.foxtelemetry.intelligence.EventCorrelationWindow;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.AnomalyCategory;
import com.foxtelemetry.model.BaseEvent;
import com.foxtelemetry.model.Severity;
import com.foxtelemetry.model.TraceEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SlowTraceRule extends BaseRule {
    private final long slowTraceThresholdMs;

    public SlowTraceRule(long slowTraceThresholdMs, long cooldownMs) {
        super(cooldownMs);
        this.slowTraceThresholdMs = Math.max(100L, slowTraceThresholdMs);
    }

    @NonNull
    @Override
    public String getId() {
        return "slow_trace";
    }

    @NonNull
    @Override
    public List<Anomaly> evaluate(@NonNull BaseEvent event, @NonNull EventCorrelationWindow window) {
        if (!(event instanceof TraceEvent)) {
            return java.util.Collections.emptyList();
        }

        TraceEvent trace = (TraceEvent) event;
        if (trace.durationMs < slowTraceThresholdMs) {
            return java.util.Collections.emptyList();
        }

        String emissionKey = getId() + "|" + window.getSessionKey() + "|" + trace.name;
        if (!shouldEmit(emissionKey, event.timestamp)) {
            return java.util.Collections.emptyList();
        }

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("traceName", trace.name);
        context.put("durationMs", trace.durationMs);
        context.put("slowTraceThresholdMs", slowTraceThresholdMs);
        context.put("status", trace.status);

        Anomaly anomaly = new Anomaly(
                "SLOW_TRACE",
                "Slow operation detected",
                Severity.WARNING,
                AnomalyCategory.PERFORMANCE,
                "Trace '" + trace.name + "' exceeded the slow threshold"
        );
        anomaly.context.putAll(context);
        return java.util.Collections.singletonList(anomaly);
    }
}
