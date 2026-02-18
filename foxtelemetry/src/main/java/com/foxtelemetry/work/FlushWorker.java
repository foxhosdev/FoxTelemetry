package com.foxtelemetry.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.foxtelemetry.FoxTelemetry;
import com.foxtelemetry.core.EventQueue;
import com.foxtelemetry.core.FoxTelemetryConfig;
import com.foxtelemetry.net.IngestClient;

import org.json.JSONObject;

import java.util.List;

public final class FlushWorker extends Worker {

    private static final String TAG = "FoxTelemetryFlush";

    public FlushWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        FoxTelemetryConfig cfg = FoxTelemetry.getConfig();
        EventQueue q = FoxTelemetry.getQueue();

        if (cfg == null || q == null) {
            return Result.success();
        }

        try {
            List<JSONObject> batch = q.peek(50);
            if (batch.isEmpty()) return Result.success();

            int code = IngestClient.sendBatch(cfg, batch);

            if (code >= 200 && code < 300) {
                q.drop(batch.size());
                return Result.success();
            }

            if (code == 429 || (code >= 500 && code < 600)) {
                return Result.retry();
            }

            Log.w(TAG, "Non-retryable HTTP " + code);
            return Result.success();

        } catch (Exception e) {
            Log.w(TAG, "Flush failed, retrying", e);
            return Result.retry();
        }
    }
}
