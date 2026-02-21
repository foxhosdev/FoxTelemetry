package com.foxtelemetry.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class FoxTelemetryConfig {
    @NonNull public final String projectId;
    @NonNull public final String appId;
    @NonNull public final String packageName;
    @NonNull public final String endpoint;
    @NonNull public final String ingestKey;
    @Nullable public final String environment;
    @Nullable public final String userId;

    public final boolean enableCrashCapture;
    public final int maxStackFrames;
    public final boolean allowHttp;

    public FoxTelemetryConfig(
            @NonNull String projectId,
            @NonNull String appId,
            @NonNull String packageName,
            @NonNull String endpoint,
            @NonNull String ingestKey,
            @Nullable String environment,
            @Nullable String userId,
            boolean enableCrashCapture,
            int maxStackFrames
    ) {
        this(projectId, appId, packageName, endpoint, ingestKey, environment, userId, enableCrashCapture, maxStackFrames, false);
    }

    public FoxTelemetryConfig(
            @NonNull String projectId,
            @NonNull String appId,
            @NonNull String packageName,
            @NonNull String endpoint,
            @NonNull String ingestKey,
            @Nullable String environment,
            @Nullable String userId,
            boolean enableCrashCapture,
            int maxStackFrames,
            boolean allowHttp
    ) {
        if (projectId == null || projectId.trim().isEmpty()) throw new IllegalArgumentException("projectId required");
        if (appId == null || appId.trim().isEmpty()) throw new IllegalArgumentException("appId required");
        if (packageName == null || packageName.trim().isEmpty()) throw new IllegalArgumentException("packageName required");
        if (endpoint == null || endpoint.trim().isEmpty()) throw new IllegalArgumentException("endpoint required");
        if (ingestKey == null || ingestKey.trim().isEmpty()) throw new IllegalArgumentException("ingestKey required");

        this.projectId = projectId;
        this.appId = appId;
        this.packageName = packageName;
        this.endpoint = endpoint;
        this.ingestKey = ingestKey;
        this.environment = environment;
        this.userId = userId;
        this.enableCrashCapture = enableCrashCapture;
        this.maxStackFrames = Math.max(1, maxStackFrames);
        this.allowHttp = allowHttp;
    }

    public FoxTelemetryConfig withUserId(@Nullable String newUserId) {
        return new FoxTelemetryConfig(
                projectId, appId, packageName, endpoint, ingestKey, environment, newUserId,
                enableCrashCapture, maxStackFrames, allowHttp
        );
    }
}
