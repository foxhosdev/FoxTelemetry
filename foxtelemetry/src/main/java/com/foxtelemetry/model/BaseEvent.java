package com.foxtelemetry.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.core.FoxTelemetryContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Base class for all telemetry events in FoxTelemetry v2.
 */
public abstract class BaseEvent {

    public final String eventId;
    public final long timestamp;
    public final String type;
    public String schemaVersion = FoxTelemetryContract.SCHEMA_VERSION;

    // Metadata added during dispatch
    public String sessionId;
    public String installId;
    public String userId;
    public String appVersion;
    public String packageName;
    public String environment;
    public String screenName;
    public String activeTraceName;
    public String networkState;
    public String sdkVersion = FoxTelemetryContract.SDK_VERSION;
    @Nullable public Long versionCode;
    @Nullable public String buildType;
    @Nullable public String flavor;
    @Nullable public String releaseChannel;
    @Nullable public String buildId;
    @Nullable public String deviceBrand;
    @Nullable public String deviceManufacturer;
    @Nullable public String deviceModel;
    @Nullable public String androidVersion;
    @Nullable public Integer androidSdkInt;
    public org.json.JSONArray breadcrumbs;

    protected BaseEvent(@NonNull String type) {
        this(type, System.currentTimeMillis());
    }

    protected BaseEvent(@NonNull String type, long timestamp) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = timestamp;
        this.type = type;
    }

    /**
     * Converts the event to a JSON object for storage and transport.
     */
    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("eventId", eventId);
        json.put("timestamp", timestamp);
        json.put("type", type);
        json.put("schemaVersion", schemaVersion);

        if (sessionId != null) json.put("sessionId", sessionId);
        if (installId != null) json.put("installId", installId);
        if (userId != null) json.put("userId", userId);
        if (appVersion != null) json.put("appVersion", appVersion);
        if (packageName != null) json.put("packageName", packageName);
        if (environment != null) json.put("environment", environment);
        if (screenName != null) json.put("screenName", screenName);
        if (activeTraceName != null) json.put("activeTraceName", activeTraceName);
        if (networkState != null) json.put("networkState", networkState);
        if (sdkVersion != null) json.put("sdkVersion", sdkVersion);
        if (versionCode != null) json.put("versionCode", versionCode.longValue());
        if (buildType != null) json.put("buildType", buildType);
        if (flavor != null) json.put("flavor", flavor);
        if (releaseChannel != null) json.put("releaseChannel", releaseChannel);
        if (buildId != null) json.put("buildId", buildId);
        if (breadcrumbs != null) json.put("breadcrumbs", breadcrumbs);
        appendReleaseMetadata(json);
        appendDeviceMetadata(json);

        return json;
    }

    private void appendReleaseMetadata(@NonNull JSONObject json) throws JSONException {
        if (appVersion == null
                && versionCode == null
                && buildType == null
                && flavor == null
                && releaseChannel == null
                && buildId == null) {
            return;
        }

        JSONObject release = new JSONObject();
        if (appVersion != null) release.put("versionName", appVersion);
        if (versionCode != null) release.put("versionCode", versionCode.longValue());
        if (buildType != null) release.put("buildType", buildType);
        if (flavor != null) release.put("flavor", flavor);
        if (releaseChannel != null) release.put("releaseChannel", releaseChannel);
        if (buildId != null) release.put("buildId", buildId);
        json.put("release", release);
    }

    private void appendDeviceMetadata(@NonNull JSONObject json) throws JSONException {
        if (deviceBrand == null
                && deviceManufacturer == null
                && deviceModel == null
                && androidVersion == null
                && androidSdkInt == null) {
            return;
        }

        JSONObject device = new JSONObject();
        if (deviceBrand != null) device.put("brand", deviceBrand);
        if (deviceManufacturer != null) device.put("manufacturer", deviceManufacturer);
        if (deviceModel != null) device.put("model", deviceModel);
        if (androidVersion != null) device.put("androidVersion", androidVersion);
        if (androidSdkInt != null) device.put("sdkInt", androidSdkInt.intValue());
        json.put("device", device);
    }
}
