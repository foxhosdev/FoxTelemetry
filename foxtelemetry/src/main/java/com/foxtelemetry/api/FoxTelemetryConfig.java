package com.foxtelemetry.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Configuration for FoxTelemetry SDK v2.
 */
public final class FoxTelemetryConfig {
    @NonNull public final String projectId;
    @NonNull public final String appId;
    @NonNull public final String packageName;
    @NonNull public final String endpoint;
    @NonNull public final String ingestKey;
    @Nullable public final String environment;
    @Nullable public final String userId;
    @Nullable public final String buildType;
    @Nullable public final String flavor;
    @Nullable public final String releaseChannel;
    @Nullable public final String buildId;
    @Nullable public final Long versionCode;

    public final boolean enableCrashCapture;
    public final boolean enableAnomalyDetection;
    public final boolean enableSessionTracking;
    public final boolean enableMainThreadMonitor;
    public final boolean enableMemoryTracking;
    public final boolean enableAutoBreadcrumbs;
    public final boolean enableDebugLogs;
    public final boolean enableRuntimeIntelligence;

    public final int maxStackFrames;
    public final int maxBreadcrumbs;
    public final int flushBatchSize;
    public final int maxQueueEventCount;
    public final long maxQueueSizeBytes;
    public final long sessionTimeoutMs;
    public final int maxCorrelationEvents;
    public final long correlationWindowMs;
    public final long slowTraceThresholdMs;
    public final long mainThreadSlowThresholdMs;
    public final long mainThreadBlockedThresholdMs;
    public final int repeatedExceptionThreshold;
    public final long repeatedExceptionWindowMs;
    public final int callbackStormThreshold;
    public final long callbackStormWindowMs;
    public final long intelligenceCooldownMs;
    public final long signalNoiseWindowMs;
    public final int maxDuplicateLogsPerWindow;
    public final int logSampleRateAfterLimit;
    public final int maxDuplicateTracksPerWindow;
    public final int trackSampleRateAfterLimit;
    public final boolean allowHttp;

    private FoxTelemetryConfig(Builder builder) {
        this.projectId = builder.projectId;
        this.appId = builder.appId;
        this.packageName = builder.packageName;
        this.endpoint = builder.endpoint;
        this.ingestKey = builder.ingestKey;
        this.environment = builder.environment;
        this.userId = builder.userId;
        this.buildType = builder.buildType;
        this.flavor = builder.flavor;
        this.releaseChannel = builder.releaseChannel;
        this.buildId = builder.buildId;
        this.versionCode = builder.versionCode;
        this.enableCrashCapture = builder.enableCrashCapture;
        this.enableAnomalyDetection = builder.enableAnomalyDetection;
        this.enableSessionTracking = builder.enableSessionTracking;
        this.enableMainThreadMonitor = builder.enableMainThreadMonitor;
        this.enableMemoryTracking = builder.enableMemoryTracking;
        this.enableAutoBreadcrumbs = builder.enableAutoBreadcrumbs;
        this.enableDebugLogs = builder.enableDebugLogs;
        this.enableRuntimeIntelligence = builder.enableRuntimeIntelligence;
        this.maxStackFrames = builder.maxStackFrames;
        this.maxBreadcrumbs = builder.maxBreadcrumbs;
        this.flushBatchSize = builder.flushBatchSize;
        this.maxQueueEventCount = builder.maxQueueEventCount;
        this.maxQueueSizeBytes = builder.maxQueueSizeBytes;
        this.sessionTimeoutMs = builder.sessionTimeoutMs;
        this.maxCorrelationEvents = builder.maxCorrelationEvents;
        this.correlationWindowMs = builder.correlationWindowMs;
        this.slowTraceThresholdMs = builder.slowTraceThresholdMs;
        this.mainThreadSlowThresholdMs = builder.mainThreadSlowThresholdMs;
        this.mainThreadBlockedThresholdMs = builder.mainThreadBlockedThresholdMs;
        this.repeatedExceptionThreshold = builder.repeatedExceptionThreshold;
        this.repeatedExceptionWindowMs = builder.repeatedExceptionWindowMs;
        this.callbackStormThreshold = builder.callbackStormThreshold;
        this.callbackStormWindowMs = builder.callbackStormWindowMs;
        this.intelligenceCooldownMs = builder.intelligenceCooldownMs;
        this.signalNoiseWindowMs = builder.signalNoiseWindowMs;
        this.maxDuplicateLogsPerWindow = builder.maxDuplicateLogsPerWindow;
        this.logSampleRateAfterLimit = builder.logSampleRateAfterLimit;
        this.maxDuplicateTracksPerWindow = builder.maxDuplicateTracksPerWindow;
        this.trackSampleRateAfterLimit = builder.trackSampleRateAfterLimit;
        this.allowHttp = builder.allowHttp;
    }

    public FoxTelemetryConfig withUserId(@Nullable String newUserId) {
        return new Builder(this).userId(newUserId).build();
    }

    public static class Builder {
        private String projectId;
        private String appId;
        private String packageName;
        private String endpoint;
        private String ingestKey;
        private String environment;
        private String userId;
        private String buildType;
        private String flavor;
        private String releaseChannel;
        private String buildId;
        private Long versionCode;

        private boolean enableCrashCapture = true;
        private boolean enableAnomalyDetection = true;
        private boolean enableSessionTracking = true;
        private boolean enableMainThreadMonitor = true;
        private boolean enableMemoryTracking = true;
        private boolean enableAutoBreadcrumbs = true;
        private boolean enableDebugLogs = false;
        private boolean enableRuntimeIntelligence = true;

        private int maxStackFrames = 50;
        private int maxBreadcrumbs = 100;
        private int flushBatchSize = 50;
        private int maxQueueEventCount = 10_000;
        private long maxQueueSizeBytes = 10L * 1024L * 1024L;
        private long sessionTimeoutMs = 30_000L;
        private int maxCorrelationEvents = 200;
        private long correlationWindowMs = 30_000L;
        private long slowTraceThresholdMs = 2_000L;
        private long mainThreadSlowThresholdMs = 400L;
        private long mainThreadBlockedThresholdMs = 3_000L;
        private int repeatedExceptionThreshold = 5;
        private long repeatedExceptionWindowMs = 10_000L;
        private int callbackStormThreshold = 20;
        private long callbackStormWindowMs = 5_000L;
        private long intelligenceCooldownMs = 60_000L;
        private long signalNoiseWindowMs = 30_000L;
        private int maxDuplicateLogsPerWindow = 3;
        private int logSampleRateAfterLimit = 10;
        private int maxDuplicateTracksPerWindow = 5;
        private int trackSampleRateAfterLimit = 5;
        private boolean allowHttp = false;

        public Builder() {}

        public Builder(@NonNull FoxTelemetryConfig config) {
            this.projectId = config.projectId;
            this.appId = config.appId;
            this.packageName = config.packageName;
            this.endpoint = config.endpoint;
            this.ingestKey = config.ingestKey;
            this.environment = config.environment;
            this.userId = config.userId;
            this.buildType = config.buildType;
            this.flavor = config.flavor;
            this.releaseChannel = config.releaseChannel;
            this.buildId = config.buildId;
            this.versionCode = config.versionCode;
            this.enableCrashCapture = config.enableCrashCapture;
            this.enableAnomalyDetection = config.enableAnomalyDetection;
            this.enableSessionTracking = config.enableSessionTracking;
            this.enableMainThreadMonitor = config.enableMainThreadMonitor;
            this.enableMemoryTracking = config.enableMemoryTracking;
            this.enableAutoBreadcrumbs = config.enableAutoBreadcrumbs;
            this.enableDebugLogs = config.enableDebugLogs;
            this.enableRuntimeIntelligence = config.enableRuntimeIntelligence;
            this.maxStackFrames = config.maxStackFrames;
            this.maxBreadcrumbs = config.maxBreadcrumbs;
            this.flushBatchSize = config.flushBatchSize;
            this.maxQueueEventCount = config.maxQueueEventCount;
            this.maxQueueSizeBytes = config.maxQueueSizeBytes;
            this.sessionTimeoutMs = config.sessionTimeoutMs;
            this.maxCorrelationEvents = config.maxCorrelationEvents;
            this.correlationWindowMs = config.correlationWindowMs;
            this.slowTraceThresholdMs = config.slowTraceThresholdMs;
            this.mainThreadSlowThresholdMs = config.mainThreadSlowThresholdMs;
            this.mainThreadBlockedThresholdMs = config.mainThreadBlockedThresholdMs;
            this.repeatedExceptionThreshold = config.repeatedExceptionThreshold;
            this.repeatedExceptionWindowMs = config.repeatedExceptionWindowMs;
            this.callbackStormThreshold = config.callbackStormThreshold;
            this.callbackStormWindowMs = config.callbackStormWindowMs;
            this.intelligenceCooldownMs = config.intelligenceCooldownMs;
            this.signalNoiseWindowMs = config.signalNoiseWindowMs;
            this.maxDuplicateLogsPerWindow = config.maxDuplicateLogsPerWindow;
            this.logSampleRateAfterLimit = config.logSampleRateAfterLimit;
            this.maxDuplicateTracksPerWindow = config.maxDuplicateTracksPerWindow;
            this.trackSampleRateAfterLimit = config.trackSampleRateAfterLimit;
            this.allowHttp = config.allowHttp;
        }

        public Builder projectId(@NonNull String projectId) { this.projectId = projectId; return this; }
        public Builder appId(@NonNull String appId) { this.appId = appId; return this; }
        public Builder packageName(@NonNull String packageName) { this.packageName = packageName; return this; }
        public Builder endpoint(@NonNull String endpoint) { this.endpoint = endpoint; return this; }
        public Builder ingestKey(@NonNull String ingestKey) { this.ingestKey = ingestKey; return this; }
        public Builder environment(@Nullable String environment) { this.environment = environment; return this; }
        public Builder userId(@Nullable String userId) { this.userId = userId; return this; }
        public Builder versionCode(long versionCode) { this.versionCode = Long.valueOf(versionCode); return this; }
        public Builder buildType(@Nullable String buildType) { this.buildType = normalizeOptional(buildType); return this; }
        public Builder flavor(@Nullable String flavor) { this.flavor = normalizeOptional(flavor); return this; }
        public Builder releaseChannel(@Nullable String releaseChannel) { this.releaseChannel = normalizeOptional(releaseChannel); return this; }
        public Builder buildId(@Nullable String buildId) { this.buildId = normalizeOptional(buildId); return this; }
        
        public Builder enableCrashCapture(boolean enabled) { this.enableCrashCapture = enabled; return this; }
        public Builder enableAnomalyDetection(boolean enabled) { this.enableAnomalyDetection = enabled; return this; }
        public Builder enableSessionTracking(boolean enabled) { this.enableSessionTracking = enabled; return this; }
        public Builder enableMainThreadMonitor(boolean enabled) { this.enableMainThreadMonitor = enabled; return this; }
        public Builder enableMemoryTracking(boolean enabled) { this.enableMemoryTracking = enabled; return this; }
        public Builder enableAutoBreadcrumbs(boolean enabled) { this.enableAutoBreadcrumbs = enabled; return this; }
        public Builder enableDebugLogs(boolean enabled) { this.enableDebugLogs = enabled; return this; }
        public Builder enableRuntimeIntelligence(boolean enabled) { this.enableRuntimeIntelligence = enabled; return this; }

        public Builder maxStackFrames(int max) { this.maxStackFrames = max; return this; }
        public Builder maxBreadcrumbs(int max) { this.maxBreadcrumbs = max; return this; }
        public Builder flushBatchSize(int size) { this.flushBatchSize = size; return this; }
        public Builder maxQueueEventCount(int count) { this.maxQueueEventCount = count; return this; }
        public Builder maxQueueSizeBytes(long bytes) { this.maxQueueSizeBytes = bytes; return this; }
        public Builder sessionTimeoutMs(long timeoutMs) { this.sessionTimeoutMs = timeoutMs; return this; }
        public Builder maxCorrelationEvents(int count) { this.maxCorrelationEvents = count; return this; }
        public Builder correlationWindowMs(long windowMs) { this.correlationWindowMs = windowMs; return this; }
        public Builder slowTraceThresholdMs(long thresholdMs) { this.slowTraceThresholdMs = thresholdMs; return this; }
        public Builder mainThreadSlowThresholdMs(long thresholdMs) { this.mainThreadSlowThresholdMs = thresholdMs; return this; }
        public Builder mainThreadBlockedThresholdMs(long thresholdMs) { this.mainThreadBlockedThresholdMs = thresholdMs; return this; }
        public Builder repeatedExceptionThreshold(int threshold) { this.repeatedExceptionThreshold = threshold; return this; }
        public Builder repeatedExceptionWindowMs(long windowMs) { this.repeatedExceptionWindowMs = windowMs; return this; }
        public Builder callbackStormThreshold(int threshold) { this.callbackStormThreshold = threshold; return this; }
        public Builder callbackStormWindowMs(long windowMs) { this.callbackStormWindowMs = windowMs; return this; }
        public Builder intelligenceCooldownMs(long cooldownMs) { this.intelligenceCooldownMs = cooldownMs; return this; }
        public Builder signalNoiseWindowMs(long windowMs) { this.signalNoiseWindowMs = windowMs; return this; }
        public Builder maxDuplicateLogsPerWindow(int count) { this.maxDuplicateLogsPerWindow = count; return this; }
        public Builder logSampleRateAfterLimit(int rate) { this.logSampleRateAfterLimit = rate; return this; }
        public Builder maxDuplicateTracksPerWindow(int count) { this.maxDuplicateTracksPerWindow = count; return this; }
        public Builder trackSampleRateAfterLimit(int rate) { this.trackSampleRateAfterLimit = rate; return this; }
        public Builder allowHttp(boolean allow) { this.allowHttp = allow; return this; }

        public FoxTelemetryConfig build() {
            if (projectId == null) throw new IllegalStateException("projectId is required");
            if (appId == null) throw new IllegalStateException("appId is required");
            if (packageName == null) throw new IllegalStateException("packageName is required");
            if (endpoint == null) throw new IllegalStateException("endpoint is required");
            if (ingestKey == null) throw new IllegalStateException("ingestKey is required");
            maxStackFrames = Math.max(1, maxStackFrames);
            maxBreadcrumbs = Math.max(1, maxBreadcrumbs);
            flushBatchSize = Math.max(1, flushBatchSize);
            maxQueueEventCount = Math.max(100, maxQueueEventCount);
            maxQueueSizeBytes = Math.max(1024L, maxQueueSizeBytes);
            sessionTimeoutMs = Math.max(1_000L, sessionTimeoutMs);
            maxCorrelationEvents = Math.max(20, maxCorrelationEvents);
            correlationWindowMs = Math.max(1_000L, correlationWindowMs);
            slowTraceThresholdMs = Math.max(100L, slowTraceThresholdMs);
            mainThreadSlowThresholdMs = Math.max(100L, mainThreadSlowThresholdMs);
            mainThreadBlockedThresholdMs = Math.max(mainThreadSlowThresholdMs + 100L, mainThreadBlockedThresholdMs);
            repeatedExceptionThreshold = Math.max(2, repeatedExceptionThreshold);
            repeatedExceptionWindowMs = Math.max(1_000L, repeatedExceptionWindowMs);
            callbackStormThreshold = Math.max(3, callbackStormThreshold);
            callbackStormWindowMs = Math.max(1_000L, callbackStormWindowMs);
            intelligenceCooldownMs = Math.max(1_000L, intelligenceCooldownMs);
            signalNoiseWindowMs = Math.max(1_000L, signalNoiseWindowMs);
            maxDuplicateLogsPerWindow = Math.max(1, maxDuplicateLogsPerWindow);
            logSampleRateAfterLimit = Math.max(1, logSampleRateAfterLimit);
            maxDuplicateTracksPerWindow = Math.max(1, maxDuplicateTracksPerWindow);
            trackSampleRateAfterLimit = Math.max(1, trackSampleRateAfterLimit);
            if (versionCode != null && versionCode.longValue() <= 0L) {
                versionCode = null;
            }
            return new FoxTelemetryConfig(this);
        }

        @Nullable
        private static String normalizeOptional(@Nullable String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}
