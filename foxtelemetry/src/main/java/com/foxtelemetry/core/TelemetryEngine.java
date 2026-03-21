package com.foxtelemetry.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.api.FoxTelemetryConfig;
import com.foxtelemetry.api.Trace;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.BaseEvent;

import java.util.Map;

public interface TelemetryEngine {

    boolean isInitialized();

    @NonNull
    FoxTelemetryConfig getConfig();

    void dispatch(@NonNull BaseEvent event);

    void report(@NonNull Throwable throwable, @Nullable String contextTag, boolean isFatal);

    void track(@NonNull String name, @Nullable Map<String, String> attributes);

    void captureAnomaly(@NonNull Anomaly anomaly);

    void addBreadcrumb(@NonNull String category, @NonNull String message);

    void setUserId(@Nullable String userId);

    @Nullable
    Trace startTrace(@NonNull String name);

    void flush();
}
