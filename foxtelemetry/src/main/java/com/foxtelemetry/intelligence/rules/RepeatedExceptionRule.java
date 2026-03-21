package com.foxtelemetry.intelligence.rules;

import androidx.annotation.NonNull;

import com.foxtelemetry.intelligence.EventCorrelationWindow;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.AnomalyCategory;
import com.foxtelemetry.model.BaseEvent;
import com.foxtelemetry.model.ErrorEvent;
import com.foxtelemetry.model.Severity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RepeatedExceptionRule extends BaseRule {
    private final int threshold;
    private final long windowMs;

    public RepeatedExceptionRule(int threshold, long windowMs, long cooldownMs) {
        super(cooldownMs);
        this.threshold = Math.max(2, threshold);
        this.windowMs = Math.max(1_000L, windowMs);
    }

    @NonNull
    @Override
    public String getId() {
        return "repeated_exception";
    }

    @NonNull
    @Override
    public List<Anomaly> evaluate(@NonNull BaseEvent event, @NonNull EventCorrelationWindow window) {
        if (!(event instanceof ErrorEvent)) {
            return java.util.Collections.emptyList();
        }

        ErrorEvent errorEvent = (ErrorEvent) event;
        if (errorEvent.isFatal) {
            return java.util.Collections.emptyList();
        }

        String signature = buildSignature(errorEvent);
        long minTimestamp = event.timestamp - windowMs;
        List<ErrorEvent> matches = new ArrayList<>();
        for (ErrorEvent candidate : window.getErrorEvents()) {
            if (candidate.isFatal || candidate.timestamp < minTimestamp) {
                continue;
            }
            if (signature.equals(buildSignature(candidate))) {
                matches.add(candidate);
            }
        }

        if (matches.size() < threshold) {
            return java.util.Collections.emptyList();
        }

        String emissionKey = getId() + "|" + window.getSessionKey() + "|" + signature;
        if (!shouldEmit(emissionKey, event.timestamp)) {
            return java.util.Collections.emptyList();
        }

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("exceptionName", errorEvent.exceptionName);
        context.put("contextTag", errorEvent.contextTag);
        context.put("count", matches.size());
        context.put("windowMs", windowMs);
        context.put("fingerprint", signature);

        Anomaly anomaly = new Anomaly(
                "REPEATED_EXCEPTION",
                "Repeated exception detected",
                matches.size() >= (threshold * 2) ? Severity.ERROR : Severity.WARNING,
                AnomalyCategory.RELIABILITY,
                "The same non-fatal exception repeated " + matches.size() + " times in a short time window"
        );
        anomaly.context.putAll(context);
        return java.util.Collections.singletonList(anomaly);
    }

    @NonNull
    private String buildSignature(@NonNull ErrorEvent errorEvent) {
        return errorEvent.exceptionName + "|" + errorEvent.contextTag + "|" + firstStackFrame(errorEvent.stackTrace);
    }

    @NonNull
    private String firstStackFrame(@NonNull String stackTrace) {
        String[] lines = stackTrace.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("at ")) {
                return trimmed;
            }
        }
        return "";
    }
}
