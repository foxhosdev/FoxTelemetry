package com.foxtelemetry.core;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

/**
 * Persists a stable install identifier for the lifetime of the app install.
 */
public final class InstallIdStore {

    private static final String PREFS_NAME = "foxtelemetry";
    private static final String KEY_INSTALL_ID = "install_id";

    private InstallIdStore() {}

    /**
     * Returns the stored installId, creating and persisting one if absent.
     * The value is never logged.
     */
    @NonNull
    public static String getOrCreateInstallId(@NonNull Context context) {
        return getOrCreateInstallId(new PrefsStorage(context.getApplicationContext()));
    }

    // Package-private for tests.
    static String getOrCreateInstallId(@NonNull Storage storage) {
        synchronized (InstallIdStore.class) {
            String existing = normalize(storage.get());
            if (existing != null) return existing;

            String id = UUID.randomUUID().toString();
            storage.set(id);
            return id;
        }
    }

    @Nullable
    private static String normalize(@Nullable String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    interface Storage {
        @Nullable String get();
        void set(@NonNull String id);
    }

    private static final class PrefsStorage implements Storage {
        private final SharedPreferences prefs;

        PrefsStorage(Context context) {
            this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        @Nullable
        @Override
        public String get() {
            return prefs.getString(KEY_INSTALL_ID, null);
        }

        @Override
        public void set(@NonNull String id) {
            prefs.edit().putString(KEY_INSTALL_ID, id).apply();
        }
    }
}
