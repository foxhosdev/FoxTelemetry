package com.foxtelemetry.core;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class TelemetryEventBuilder {

    private TelemetryEventBuilder() {}

    public static JSONObject buildErrorEvent(FoxTelemetryConfig cfg, String installId, Throwable t, String contextTag) throws Exception {
        JSONObject root = base(cfg, installId);
        root.put("type", "error");
        root.put("context", contextTag);
        root.put("exception", buildThrowable(t, cfg.maxStackFrames));
        return root;
    }

    public static JSONObject buildLogEvent(FoxTelemetryConfig cfg, String installId, String level, String tag, String message) throws Exception {
        JSONObject root = base(cfg, installId);
        root.put("type", "log");
        root.put("level", level);
        root.put("tag", tag);
        root.put("message", message);
        return root;
    }

    private static JSONObject base(FoxTelemetryConfig cfg, String installId) throws Exception {
        JSONObject root = new JSONObject();
        root.put("timestamp", System.currentTimeMillis());

        root.put("projectId", cfg.projectId);
        root.put("appId", cfg.appId);
        root.put("packageName", cfg.packageName);
        root.put("installId", installId);

        if (cfg.environment != null) root.put("environment", cfg.environment);
        if (cfg.userId != null) root.put("userId", cfg.userId);

        JSONObject device = new JSONObject();
        device.put("brand", Build.BRAND);
        device.put("model", Build.MODEL);
        device.put("sdkInt", Build.VERSION.SDK_INT);
        device.put("release", Build.VERSION.RELEASE);
        root.put("device", device);

        return root;
    }

    private static JSONObject buildThrowable(Throwable t, int maxFrames) throws Exception {
        JSONObject ex = new JSONObject();
        ex.put("name", t.getClass().getName());
        ex.put("message", t.getMessage());

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        ex.put("stacktrace", sw.toString());

        StackTraceElement[] st = t.getStackTrace();
        int n = Math.min(st.length, maxFrames);

        JSONArray frames = new JSONArray();
        for (int i = 0; i < n; i++) {
            StackTraceElement e = st[i];
            JSONObject f = new JSONObject();
            f.put("class", e.getClassName());
            f.put("method", e.getMethodName());
            f.put("file", e.getFileName());
            f.put("line", e.getLineNumber());
            f.put("native", e.isNativeMethod());
            frames.put(f);
        }
        ex.put("frames", frames);

        if (t.getCause() != null && t.getCause() != t) {
            ex.put("cause", buildThrowable(t.getCause(), maxFrames));
        }
        return ex;
    }
}
