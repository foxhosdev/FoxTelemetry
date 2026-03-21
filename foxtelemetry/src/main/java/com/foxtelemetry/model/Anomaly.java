package com.foxtelemetry.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Anomaly {
    @NonNull public final String code;
    @NonNull public final String title;
    @NonNull public final AnomalyCategory category;
    @NonNull public final Severity severity;
    @NonNull public final String message;
    public final long timestamp;
    @NonNull public final String fingerprint;
    public int occurrenceCount = 1;
    @Nullable public String sessionId;
    @Nullable public String installId;
    @NonNull public final Map<String, Object> context;

    public Anomaly(@NonNull String code,
                   @NonNull String title,
                   @NonNull Severity severity,
                   @NonNull AnomalyCategory category,
                   @NonNull String message) {
        this(code, title, severity, category, message, System.currentTimeMillis(), null);
    }

    public Anomaly(@NonNull String code,
                   @NonNull String title,
                   @NonNull Severity severity,
                   @NonNull AnomalyCategory category,
                   @NonNull String message,
                   long timestamp,
                   @Nullable String fingerprint) {
        this.code = requireValue(code, "code");
        this.title = requireValue(title, "title");
        this.severity = severity;
        this.category = category;
        this.message = requireValue(message, "message");
        this.timestamp = timestamp;
        this.fingerprint = fingerprint != null && !fingerprint.trim().isEmpty()
                ? fingerprint
                : generateFingerprint(this.code, this.category, this.severity, this.message);
        this.context = new LinkedHashMap<>();
    }

    @NonNull
    public static String generateFingerprint(@NonNull String code,
                                             @NonNull AnomalyCategory category,
                                             @NonNull Severity severity,
                                             @NonNull String message) {
        return code + ":" + category.name() + ":" + severity.name() + ":" + Integer.toHexString(message.hashCode());
    }

    @NonNull
    private static String requireValue(@Nullable String value, @NonNull String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}
