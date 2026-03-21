package com.foxtelemetry.model;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorEvent extends BaseEvent {
    public final String exceptionName;
    public final String stackTrace;
    public final String contextTag;
    public final boolean isFatal;

    public ErrorEvent(@NonNull Throwable throwable, @Nullable String contextTag, boolean isFatal) {
        super("error");
        this.exceptionName = throwable.getClass().getName();
        this.stackTrace = getStackTraceString(throwable);
        this.contextTag = contextTag != null ? contextTag : "unknown";
        this.isFatal = isFatal;
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();
        json.put("exceptionName", exceptionName);
        json.put("stackTrace", stackTrace);
        json.put("contextTag", contextTag);
        json.put("isFatal", isFatal);
        return json;
    }

    private static String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
