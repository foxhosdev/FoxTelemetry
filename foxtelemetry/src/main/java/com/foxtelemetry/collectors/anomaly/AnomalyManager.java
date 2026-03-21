package com.foxtelemetry.collectors.anomaly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.model.Anomaly;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages anomaly detection, deduplication, and counting.
 */
public final class AnomalyManager {
    private final Map<String, AnomalyState> states = new HashMap<>();

    public synchronized boolean shouldReport(@NonNull Anomaly anomaly, @Nullable String sessionId, @Nullable String installId) {
        anomaly.sessionId = sessionId;
        anomaly.installId = installId;

        String fingerprintKey = buildKey(anomaly.fingerprint, sessionId, installId);
        AnomalyState state = states.get(fingerprintKey);
        if (state == null) {
            states.put(fingerprintKey, new AnomalyState());
            anomaly.occurrenceCount = 1;
            return true;
        }

        state.count++;
        anomaly.occurrenceCount = state.count;
        return false;
    }

    private String buildKey(@NonNull String fingerprint, @Nullable String sessionId, @Nullable String installId) {
        String scope = sessionId != null && !sessionId.trim().isEmpty()
                ? sessionId
                : (installId != null && !installId.trim().isEmpty() ? installId : "_global");
        return scope + "|" + fingerprint;
    }

    private static class AnomalyState {
        int count = 1;
    }
}
