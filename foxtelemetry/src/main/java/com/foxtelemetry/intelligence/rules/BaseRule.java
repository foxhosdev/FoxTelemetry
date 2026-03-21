package com.foxtelemetry.intelligence.rules;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

abstract class BaseRule implements Rule {
    private final long cooldownMs;
    private final Map<String, Long> lastEmittedAt = new HashMap<>();

    BaseRule(long cooldownMs) {
        this.cooldownMs = Math.max(1_000L, cooldownMs);
    }

    protected synchronized boolean shouldEmit(@NonNull String key, long nowMs) {
        Long previous = lastEmittedAt.get(key);
        if (previous != null && (nowMs - previous) < cooldownMs) {
            return false;
        }
        lastEmittedAt.put(key, nowMs);
        return true;
    }
}
