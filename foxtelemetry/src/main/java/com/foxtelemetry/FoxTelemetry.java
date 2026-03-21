package com.foxtelemetry;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.foxtelemetry.core.FoxTelemetryConfig;
import com.foxtelemetry.work.FlushWorker;

@Deprecated
public final class FoxTelemetry {

    private static final String WORK_NAME = "foxtelemetry-flush";

    private FoxTelemetry() {}

    /**
     * Legacy manual init kept as a compatibility adapter over the v2 API.
     */
    public static void init(@NonNull Context context, @NonNull FoxTelemetryConfig cfg) {
        com.foxtelemetry.api.FoxTelemetry.init(context, cfg.toPublicConfig());
    }

    public static boolean isInitialized() {
        return com.foxtelemetry.api.FoxTelemetry.isInitialized();
    }

    public static void setUserId(@Nullable String userId) {
        com.foxtelemetry.api.FoxTelemetry.setUserId(userId);
    }

    public static void d(@NonNull String tag, @NonNull String message) {
        com.foxtelemetry.api.FoxTelemetry.d(tag, message);
    }

    public static void i(@NonNull String tag, @NonNull String message) {
        com.foxtelemetry.api.FoxTelemetry.i(tag, message);
    }

    public static void w(@NonNull String tag, @NonNull String message) {
        com.foxtelemetry.api.FoxTelemetry.w(tag, message);
    }

    public static void e(@NonNull String tag, @NonNull String message) {
        com.foxtelemetry.api.FoxTelemetry.e(tag, message);
    }

    public static void report(@NonNull Throwable t, @NonNull String contextTag) {
        com.foxtelemetry.api.FoxTelemetry.report(t, contextTag);
    }

    public static void flush() {
        com.foxtelemetry.api.FoxTelemetry.flush();
    }

    /**
     * Request a background flush via WorkManager.
     */
    public static void flushAsync(@NonNull Context context) {
        if (context == null) {
            return;
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(FlushWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, req);
    }
}
