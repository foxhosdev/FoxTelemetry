package com.foxtelemetry.intelligence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.model.AnomalyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InsightEngine {
    private final long cooldownMs;
    private final Map<String, Long> lastEmittedAt = new HashMap<>();

    public InsightEngine(long cooldownMs) {
        this.cooldownMs = Math.max(1_000L, cooldownMs);
    }

    @NonNull
    public synchronized List<Insight> evaluate(@Nullable String sessionId,
                                               @NonNull List<AnomalyEvent> anomalies,
                                               long nowMs) {
        boolean hasSlowTrace = false;
        boolean hasCallbackStorm = false;
        boolean hasRepeatedException = false;

        for (AnomalyEvent anomalyEvent : anomalies) {
            String code = anomalyEvent.anomaly.code;
            if ("SLOW_TRACE".equals(code)) {
                hasSlowTrace = true;
            } else if ("CALLBACK_STORM".equals(code)) {
                hasCallbackStorm = true;
            } else if ("REPEATED_EXCEPTION".equals(code)) {
                hasRepeatedException = true;
            }
        }

        List<Insight> outputs = new ArrayList<>();

        if (hasSlowTrace && shouldEmit(key(sessionId, "performance_issue"), nowMs)) {
            outputs.add(new Insight(
                    "performance_issue",
                    "Slow operation detected",
                    "A runtime operation exceeded the expected duration threshold",
                    "operation is taking longer than expected",
                    "user experience degradation",
                    0.75d,
                    Arrays.asList("SLOW_TRACE")
            ));
        }

        if (hasSlowTrace && hasCallbackStorm && shouldEmit(key(sessionId, "runtime_loop_issue"), nowMs)) {
            outputs.add(new Insight(
                    "runtime_loop_issue",
                    "Possible callback loop causing slowdown",
                    "Repeated callback activity appears correlated with a slow operation",
                    "repeated callbacks causing excessive processing",
                    "performance degradation",
                    0.88d,
                    Arrays.asList("CALLBACK_STORM", "SLOW_TRACE")
            ));
        }

        if (hasRepeatedException && shouldEmit(key(sessionId, "unstable_flow"), nowMs)) {
            outputs.add(new Insight(
                    "unstable_flow",
                    "Repeated exception detected",
                    "The same failure pattern is recurring in the current flow",
                    "unstable code path",
                    "potential crashes or broken user flow",
                    0.82d,
                    Arrays.asList("REPEATED_EXCEPTION")
            ));
        }

        return outputs;
    }

    private boolean shouldEmit(@NonNull String key, long nowMs) {
        Long previous = lastEmittedAt.get(key);
        if (previous != null && (nowMs - previous) < cooldownMs) {
            return false;
        }
        lastEmittedAt.put(key, nowMs);
        return true;
    }

    @NonNull
    private String key(@Nullable String sessionId, @NonNull String insightType) {
        String scope = (sessionId != null && !sessionId.trim().isEmpty()) ? sessionId : "_global";
        return scope + "|" + insightType;
    }
}
