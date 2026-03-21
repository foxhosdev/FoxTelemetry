package com.foxtelemetry.core;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.api.FoxTelemetryConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class ConfigLoader {

    private ConfigLoader() {}

    @Nullable
    public static FoxTelemetryConfig loadFromAssets(@NonNull Context context, @NonNull String assetFileName) {
        try {
            AssetManager am = context.getAssets();
            try (InputStream is = am.open(assetFileName);
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject root = new JSONObject(sb.toString());
                JSONObject fox = root.getJSONObject("foxTelemetry");

                String projectId = fox.getString("projectId");
                String appId = fox.getString("appId");
                String packageName = fox.getString("packageName");
                String endpoint = fox.getString("endpoint");
                String ingestKey = fox.getString("ingestKey");
                String environment = fox.optString("environment", null);
                Long versionCode = fox.has("versionCode") ? Long.valueOf(fox.optLong("versionCode")) : null;
                String buildType = fox.optString("buildType", null);
                String flavor = fox.optString("flavor", null);
                String releaseChannel = fox.optString("releaseChannel", null);
                String buildId = fox.optString("buildId", null);
                boolean allowHttp = fox.optBoolean("allowHttp", false);
                boolean enableCrashCapture = fox.optBoolean("enableCrashCapture", true);
                boolean enableAutoBreadcrumbs = fox.optBoolean("enableAutoBreadcrumbs", true);
                boolean enableSessionTracking = fox.optBoolean("enableSessionTracking", true);
                boolean enableMainThreadMonitor = fox.optBoolean("enableMainThreadMonitor", true);
                boolean enableMemoryTracking = fox.optBoolean("enableMemoryTracking", true);
                boolean enableAnomalyDetection = fox.optBoolean("enableAnomalyDetection", true);
                boolean enableDebugLogs = fox.optBoolean("enableDebugLogs", false);
                boolean enableRuntimeIntelligence = fox.optBoolean("enableRuntimeIntelligence", true);
                int maxStackFrames = fox.optInt("maxStackFrames", 80);
                int maxBreadcrumbs = fox.optInt("maxBreadcrumbs", 100);
                int flushBatchSize = fox.optInt("flushBatchSize", 50);
                int maxQueueEventCount = fox.optInt("maxQueueEventCount", 10_000);
                long maxQueueSizeBytes = fox.optLong("maxQueueSizeBytes", 10L * 1024L * 1024L);
                long sessionTimeoutMs = fox.optLong("sessionTimeoutMs", 30_000L);
                int maxCorrelationEvents = fox.optInt("maxCorrelationEvents", 200);
                long correlationWindowMs = fox.optLong("correlationWindowMs", 30_000L);
                long slowTraceThresholdMs = fox.optLong("slowTraceThresholdMs", 2_000L);
                long mainThreadSlowThresholdMs = fox.optLong("mainThreadSlowThresholdMs", 400L);
                long mainThreadBlockedThresholdMs = fox.optLong("mainThreadBlockedThresholdMs", 3_000L);
                int repeatedExceptionThreshold = fox.optInt("repeatedExceptionThreshold", 5);
                long repeatedExceptionWindowMs = fox.optLong("repeatedExceptionWindowMs", 10_000L);
                int callbackStormThreshold = fox.optInt("callbackStormThreshold", 20);
                long callbackStormWindowMs = fox.optLong("callbackStormWindowMs", 5_000L);
                long intelligenceCooldownMs = fox.optLong("intelligenceCooldownMs", 60_000L);
                long signalNoiseWindowMs = fox.optLong("signalNoiseWindowMs", 30_000L);
                int maxDuplicateLogsPerWindow = fox.optInt("maxDuplicateLogsPerWindow", 3);
                int logSampleRateAfterLimit = fox.optInt("logSampleRateAfterLimit", 10);
                int maxDuplicateTracksPerWindow = fox.optInt("maxDuplicateTracksPerWindow", 5);
                int trackSampleRateAfterLimit = fox.optInt("trackSampleRateAfterLimit", 5);

                FoxTelemetryConfig.Builder builder = new FoxTelemetryConfig.Builder()
                        .projectId(projectId)
                        .appId(appId)
                        .packageName(packageName)
                        .endpoint(endpoint)
                        .ingestKey(ingestKey)
                        .environment(environment)
                        .buildType(buildType)
                        .flavor(flavor)
                        .releaseChannel(releaseChannel)
                        .buildId(buildId)
                        .enableCrashCapture(enableCrashCapture)
                        .enableAutoBreadcrumbs(enableAutoBreadcrumbs)
                        .enableSessionTracking(enableSessionTracking)
                        .enableMainThreadMonitor(enableMainThreadMonitor)
                        .enableMemoryTracking(enableMemoryTracking)
                        .enableAnomalyDetection(enableAnomalyDetection)
                        .enableDebugLogs(enableDebugLogs)
                        .enableRuntimeIntelligence(enableRuntimeIntelligence)
                        .maxStackFrames(maxStackFrames)
                        .maxBreadcrumbs(maxBreadcrumbs)
                        .flushBatchSize(flushBatchSize)
                        .maxQueueEventCount(maxQueueEventCount)
                        .maxQueueSizeBytes(maxQueueSizeBytes)
                        .sessionTimeoutMs(sessionTimeoutMs)
                        .maxCorrelationEvents(maxCorrelationEvents)
                        .correlationWindowMs(correlationWindowMs)
                        .slowTraceThresholdMs(slowTraceThresholdMs)
                        .mainThreadSlowThresholdMs(mainThreadSlowThresholdMs)
                        .mainThreadBlockedThresholdMs(mainThreadBlockedThresholdMs)
                        .repeatedExceptionThreshold(repeatedExceptionThreshold)
                        .repeatedExceptionWindowMs(repeatedExceptionWindowMs)
                        .callbackStormThreshold(callbackStormThreshold)
                        .callbackStormWindowMs(callbackStormWindowMs)
                        .intelligenceCooldownMs(intelligenceCooldownMs)
                        .signalNoiseWindowMs(signalNoiseWindowMs)
                        .maxDuplicateLogsPerWindow(maxDuplicateLogsPerWindow)
                        .logSampleRateAfterLimit(logSampleRateAfterLimit)
                        .maxDuplicateTracksPerWindow(maxDuplicateTracksPerWindow)
                        .trackSampleRateAfterLimit(trackSampleRateAfterLimit)
                        .allowHttp(allowHttp);
                if (versionCode != null) {
                    builder.versionCode(versionCode.longValue());
                }
                return builder.build();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
