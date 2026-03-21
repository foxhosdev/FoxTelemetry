package com.foxtelemetry.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    public String schemaVersion = "2.0";

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
    public String sdkVersion = "2.0.0";
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
        if (breadcrumbs != null) json.put("breadcrumbs", breadcrumbs);

        return json;
    }
}
