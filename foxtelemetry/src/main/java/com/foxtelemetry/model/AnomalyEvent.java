package com.foxtelemetry.model;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public final class AnomalyEvent extends BaseEvent {
    @NonNull public final Anomaly anomaly;

    public AnomalyEvent(@NonNull Anomaly anomaly) {
        super("anomaly", anomaly.timestamp);
        this.anomaly = anomaly;
    }

    public void syncAnomalyIdentity() {
        anomaly.sessionId = sessionId;
        anomaly.installId = installId;
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();
        json.put("code", anomaly.code);
        json.put("title", anomaly.title);
        json.put("severity", anomaly.severity.name());
        json.put("category", anomaly.category.name());
        json.put("message", anomaly.message);
        json.put("timestamp", anomaly.timestamp);
        json.put("fingerprint", anomaly.fingerprint);
        json.put("occurrenceCount", anomaly.occurrenceCount);
        if (anomaly.sessionId != null) json.put("anomalySessionId", anomaly.sessionId);
        if (anomaly.installId != null) json.put("anomalyInstallId", anomaly.installId);

        JSONObject ctxJson = new JSONObject();
        for (Map.Entry<String, Object> entry : anomaly.context.entrySet()) {
            ctxJson.put(entry.getKey(), JSONObject.wrap(entry.getValue()));
        }
        json.put("context", ctxJson);
        return json;
    }
}
