package com.foxtelemetry.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.core.FoxCore;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.LogEvent;

import java.util.Collections;
import java.util.Map;

/**
 * Public facade for FoxTelemetry SDK v2.
 */
public final class FoxTelemetry {
    private static final String TAG = "FoxTelemetry";

    private FoxTelemetry() {}

    /**
     * Initializes the SDK with the provided configuration.
     * This should be called in your Application.onCreate().
     */
    public static void init(@NonNull Context context, @NonNull FoxTelemetryConfig config) {
        if (context == null) throw new IllegalArgumentException("context is null");
        if (config == null) throw new IllegalArgumentException("config is null");
        FoxCore.init(context, config);
    }

    /**
     * Report a caught exception.
     */
    public static void report(@NonNull Throwable throwable) {
        report(throwable, null);
    }

    /**
     * Report a caught exception with a context tag.
     */
    public static void report(@NonNull Throwable throwable, @Nullable String contextTag) {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.report(throwable, contextTag, false);
        } else {
            Log.w(TAG, "Not initialized. Call init() first.");
        }
    }

    /**
     * Track a custom runtime signal.
     */
    public static void track(@NonNull String name) {
        track(name, Collections.<String, String>emptyMap());
    }

    /**
     * Track a custom runtime signal with attributes.
     */
    public static void track(@NonNull String name, @NonNull Map<String, String> attributes) {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.track(name, attributes);
        } else {
            Log.w(TAG, "Not initialized. Call init() first.");
        }
    }

    /**
     * Log a message.
     */
    public static void d(@NonNull String tag, @NonNull String message) { log("DEBUG", tag, message); }
    public static void i(@NonNull String tag, @NonNull String message) { log("INFO", tag, message); }
    public static void w(@NonNull String tag, @NonNull String message) { log("WARN", tag, message); }
    public static void e(@NonNull String tag, @NonNull String message) { log("ERROR", tag, message); }

    private static void log(@NonNull String level, @NonNull String tag, @NonNull String message) {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.dispatch(new LogEvent(level, tag, message));
        } else {
            Log.w(TAG, "Not initialized. Call init() first.");
        }
    }

    /**
     * Track a manual anomaly.
     */
    public static void anomaly(@NonNull Anomaly anomaly) {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.captureAnomaly(anomaly);
        } else {
            Log.w(TAG, "Not initialized. Call init() first.");
        }
    }

    /**
     * Add a breadcrumb to the current session.
     */
    public static void breadcrumb(@NonNull String category, @NonNull String message) {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.addBreadcrumb(category, message);
        } else {
            Log.w(TAG, "Not initialized. Call init() first.");
        }
    }

    /**
     * Set the current user ID.
     */
    public static void setUserId(@Nullable String userId) {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.setUserId(userId);
        } else {
            Log.w(TAG, "Not initialized. Call init() first.");
        }
    }

    /**
     * Performance tracing.
     */
    public static Trace startTrace(@NonNull String name) {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            return core.startTrace(name);
        }
        Log.w(TAG, "Not initialized. Call init() first.");
        return null;
    }

    /**
     * Placeholder for future memory probes.
     *
     * This API is intentionally unsupported until the SDK can emit
     * real memory measurements with a stable contract.
     */
    @Deprecated
    public static void measureMemory(@NonNull String label, @NonNull Runnable action) {
        throw new UnsupportedOperationException(
                "measureMemory() is not implemented yet. "
                        + "Use custom traces or track() until stable memory probes are added."
        );
    }

    /**
     * Request a background flush of events.
     */
    public static void flush() {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.flush();
        } else {
            Log.w(TAG, "Not initialized. Call init() first.");
        }
    }

    public static boolean isInitialized() {
        FoxCore core = FoxCore.getInstance();
        return core != null && core.isInitialized();
    }
}
