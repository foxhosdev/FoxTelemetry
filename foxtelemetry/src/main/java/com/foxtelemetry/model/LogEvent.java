package com.foxtelemetry.model;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public class LogEvent extends BaseEvent {
    public final String level;
    public final String tag;
    public final String message;

    public LogEvent(@NonNull String level, @NonNull String tag, @NonNull String message) {
        super("log");
        this.level = level;
        this.tag = tag;
        this.message = message;
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();
        json.put("level", level);
        json.put("tag", tag);
        json.put("message", message);
        return json;
    }
}
