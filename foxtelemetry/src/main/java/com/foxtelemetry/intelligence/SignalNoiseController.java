package com.foxtelemetry.intelligence;

import androidx.annotation.NonNull;

import com.foxtelemetry.model.BaseEvent;
import com.foxtelemetry.model.LogEvent;
import com.foxtelemetry.model.TrackEvent;

import java.util.HashMap;
import java.util.Map;

public final class SignalNoiseController {
    private final long windowMs;
    private final int maxDuplicateLogsPerWindow;
    private final int logSampleRateAfterLimit;
    private final int maxDuplicateTracksPerWindow;
    private final int trackSampleRateAfterLimit;
    private final Map<String, SignalState> states = new HashMap<>();

    public SignalNoiseController(long windowMs,
                                 int maxDuplicateLogsPerWindow,
                                 int logSampleRateAfterLimit,
                                 int maxDuplicateTracksPerWindow,
                                 int trackSampleRateAfterLimit) {
        this.windowMs = Math.max(1_000L, windowMs);
        this.maxDuplicateLogsPerWindow = Math.max(1, maxDuplicateLogsPerWindow);
        this.logSampleRateAfterLimit = Math.max(1, logSampleRateAfterLimit);
        this.maxDuplicateTracksPerWindow = Math.max(1, maxDuplicateTracksPerWindow);
        this.trackSampleRateAfterLimit = Math.max(1, trackSampleRateAfterLimit);
    }

    public synchronized boolean shouldStore(@NonNull BaseEvent event) {
        if (event instanceof LogEvent) {
            LogEvent logEvent = (LogEvent) event;
            String signature = "log|" + logEvent.level + "|" + logEvent.tag + "|" + logEvent.message;
            return shouldStore(signature, event.timestamp, maxDuplicateLogsPerWindow, logSampleRateAfterLimit);
        }
        if (event instanceof TrackEvent) {
            TrackEvent trackEvent = (TrackEvent) event;
            String signature = "track|" + trackEvent.name + "|" + trackEvent.attributes.toString();
            return shouldStore(signature, event.timestamp, maxDuplicateTracksPerWindow, trackSampleRateAfterLimit);
        }
        return true;
    }

    private boolean shouldStore(@NonNull String signature,
                                long nowMs,
                                int maxDuplicates,
                                int sampleRateAfterLimit) {
        SignalState state = states.get(signature);
        if (state == null || (nowMs - state.windowStartedAtMs) > windowMs) {
            states.put(signature, new SignalState(nowMs));
            return true;
        }

        state.count++;
        if (state.count <= maxDuplicates) {
            return true;
        }

        return ((state.count - maxDuplicates) % sampleRateAfterLimit) == 0;
    }

    private static final class SignalState {
        final long windowStartedAtMs;
        int count = 1;

        SignalState(long windowStartedAtMs) {
            this.windowStartedAtMs = windowStartedAtMs;
        }
    }
}
