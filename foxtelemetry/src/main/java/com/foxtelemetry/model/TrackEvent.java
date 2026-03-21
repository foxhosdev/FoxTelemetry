package com.foxtelemetry.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TrackEvent extends BaseEvent {
    public final String name;
    public final Map<String, String> attributes;

    public TrackEvent(@NonNull String name, @Nullable Map<String, String> attributes) {
        super("track");
        this.name = name;
        this.attributes = new LinkedHashMap<>();
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();
        json.put("name", name);

        JSONObject attrs = new JSONObject();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            attrs.put(entry.getKey(), entry.getValue());
        }
        json.put("attributes", attrs);
        return json;
    }
}
