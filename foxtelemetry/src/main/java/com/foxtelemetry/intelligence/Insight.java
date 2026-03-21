package com.foxtelemetry.intelligence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.model.BaseEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class Insight extends BaseEvent {
    /**
     * Diagnostic type. BaseEvent.type remains "insight" for the transport event kind.
     */
    @NonNull public final String type;
    @NonNull public final String title;
    @NonNull public final String summary;
    @Nullable public final String probableCause;
    @Nullable public final String impact;
    public final double confidence;
    @NonNull public final List<String> relatedAnomalyCodes;

    public Insight(@NonNull String type,
                   @NonNull String title,
                   @NonNull String summary,
                   @Nullable String probableCause,
                   @Nullable String impact,
                   double confidence,
                   @Nullable List<String> relatedAnomalyCodes) {
        super("insight");
        this.type = require(type, "type");
        this.title = require(title, "title");
        this.summary = require(summary, "summary");
        this.probableCause = probableCause;
        this.impact = impact;
        this.confidence = Math.max(0.0d, Math.min(1.0d, confidence));
        this.relatedAnomalyCodes = relatedAnomalyCodes != null
                ? new ArrayList<>(relatedAnomalyCodes)
                : new ArrayList<String>();
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();
        json.put("insightType", type);
        json.put("title", title);
        json.put("summary", summary);
        json.put("confidence", confidence);

        if (probableCause != null) {
            json.put("probableCause", probableCause);
        }
        if (impact != null) {
            json.put("impact", impact);
        }

        JSONArray codes = new JSONArray();
        for (String code : relatedAnomalyCodes) {
            codes.put(code);
        }
        json.put("relatedAnomalyCodes", codes);
        return json;
    }

    @NonNull
    private static String require(@Nullable String value, @NonNull String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}
