package com.foxtelemetry.init;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.FoxTelemetry;
import com.foxtelemetry.core.ConfigLoader;
import com.foxtelemetry.core.FoxTelemetryConfig;

/**
 * Auto-initializes FoxTelemetry if app includes assets/foxtelemetry.json
 */
public final class FoxTelemetryInitProvider extends ContentProvider {

    private static final String TAG = "FoxTelemetryInit";

    @Override
    public boolean onCreate() {
        Context ctx = getContext();
        if (ctx == null) return true;

        FoxTelemetryConfig cfg = ConfigLoader.loadFromAssets(ctx, "foxtelemetry.json");
        if (cfg == null) {
            Log.i(TAG, "assets/foxtelemetry.json not found. Skipping auto-init.");
            return true;
        }

        try {
            if (!ctx.getPackageName().equals(cfg.packageName)) {
                Log.w(TAG, "Package mismatch. Expected " + cfg.packageName + " but got " + ctx.getPackageName());
                return true;
            }
            FoxTelemetry.init(ctx, cfg);
            Log.i(TAG, "FoxTelemetry initialized.");
        } catch (Throwable t) {
            Log.e(TAG, "Auto-init failed", t);
        }
        return true;
    }

    @Nullable @Override public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) { return null; }
    @Nullable @Override public String getType(@NonNull Uri uri) { return null; }
    @Nullable @Override public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) { return null; }
    @Override public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }
    @Override public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }
}
