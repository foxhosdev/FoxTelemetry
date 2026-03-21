package com.foxtelemetry.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TraceEvent extends BaseEvent {
    public final String name;
    public final long durationMs;
    public final String status;
    public final Map<String, String> tags;

    public TraceEvent(@NonNull String name,
                      long durationMs,
                      @Nullable String status,
                      @Nullable Map<String, String> tags) {
        super("trace");
        this.name = name;
        this.durationMs = Math.max(0L, durationMs);
        this.status = status != null ? status : "ok";
        this.tags = new LinkedHashMap<>();
        if (tags != null) {
            this.tags.putAll(tags);
        }
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();
        json.put("name", name);
        json.put("durationMs", durationMs);
        json.put("status", status);

        JSONObject tagJson = new JSONObject();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            tagJson.put(entry.getKey(), entry.getValue());
        }
        json.put("tags", tagJson);
        return json;
    }
}
