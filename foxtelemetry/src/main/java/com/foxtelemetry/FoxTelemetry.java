package com.foxtelemetry;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.foxtelemetry.core.CrashHandler;
import com.foxtelemetry.core.EventQueue;
import com.foxtelemetry.core.FoxTelemetryConfig;
import com.foxtelemetry.core.TelemetryEventBuilder;
import com.foxtelemetry.work.FlushWorker;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FoxTelemetry {

    private static final String INTERNAL_TAG = "FoxTelemetry";
    private static final String WORK_NAME = "foxtelemetry-flush";

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private static volatile FoxTelemetryConfig config;
    private static volatile String installId;
    private static volatile EventQueue queue;

    private FoxTelemetry() {}

    /** Manual init (optional). Auto-init runs via ContentProvider if assets/foxtelemetry.json exists. */
    public static void init(@NonNull Context context, @NonNull FoxTelemetryConfig cfg) {
        if (context == null) throw new IllegalArgumentException("context is null");
        if (cfg == null) throw new IllegalArgumentException("config is null");

        Context app = context.getApplicationContext();
        config = cfg;

        if (installId == null) installId = UUID.randomUUID().toString();
        if (queue == null) queue = new EventQueue(app);

        if (initialized.compareAndSet(false, true)) {
            if (cfg.enableCrashCapture) {
                Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
                Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(prev));
            }
        }

        flushAsync(app);
    }

    public static boolean isInitialized() {
        return config != null && queue != null;
    }

    public static void setUserId(@Nullable String userId) {
        FoxTelemetryConfig cfg = config;
        if (cfg == null) return;
        config = cfg.withUserId(userId);
    }

    public static void d(@NonNull String tag, @NonNull String message) { emitLog("DEBUG", tag, message); }
    public static void i(@NonNull String tag, @NonNull String message) { emitLog("INFO", tag, message); }
    public static void w(@NonNull String tag, @NonNull String message) { emitLog("WARN", tag, message); }
    public static void e(@NonNull String tag, @NonNull String message) { emitLog("ERROR", tag, message); }

    public static void report(@NonNull Throwable t, @NonNull String contextTag) {
        FoxTelemetryConfig cfg = config;
        EventQueue q = queue;
        if (cfg == null || q == null) {
            Log.w(INTERNAL_TAG, "Not initialized (missing foxtelemetry.json?)");
            return;
        }
        if (t == null) return;

        try {
            JSONObject event = TelemetryEventBuilder.buildErrorEvent(cfg, installId, t, contextTag);
            q.enqueue(event);
            flushAsync(q.getContext());
        } catch (Exception ex) {
            Log.e(INTERNAL_TAG, "Failed to build/enqueue error event", ex);
        }
    }

    private static void emitLog(String level, String tag, String message) {
        FoxTelemetryConfig cfg = config;
        EventQueue q = queue;
        if (cfg == null || q == null) return;

        if ("ERROR".equals(level)) Log.e(tag, message);
        else if ("WARN".equals(level)) Log.w(tag, message);
        else if ("INFO".equals(level)) Log.i(tag, message);
        else Log.d(tag, message);

        try {
            JSONObject event = TelemetryEventBuilder.buildLogEvent(cfg, installId, level, tag, message);
            q.enqueue(event);
        } catch (Exception ignored) {}
    }

    /** Request a background flush via WorkManager. */
    public static void flushAsync(@NonNull Context context) {
        if (context == null) return;

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(FlushWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, req);
    }

    // Internal access for worker
    public static FoxTelemetryConfig getConfig() { return config; }
    public static EventQueue getQueue() { return queue; }
    static String getInstallId() { return installId; }
}
