package com.foxtelemetry.core;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistent queue backed by SQLite so events survive app restarts and
 * intermittent network/API downtime.
 */
public final class EventQueue {

    // Queue hard limits
    private static final int MAX_EVENTS = 10_000;
    private static final long MAX_BYTES = 10L * 1024L * 1024L; // 10 MB
    private static final String LOG_TAG = "FoxTelemetryQueue";

    private static final String DB_NAME = "foxtelemetry.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_EVENTS = "events";

    private final Context context;
    private final DB helper;
    private final Object lock = new Object();

    public EventQueue(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.helper = new DB(this.context);
        migrateFromLegacyFileIfPresent();
    }

    public Context getContext() { return context; }

    public void enqueue(@NonNull JSONObject event) throws Exception {
        synchronized (lock) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL(
                    "INSERT INTO " + TABLE_EVENTS + " (payload) VALUES (?)",
                    new Object[]{event.toString()});
            enforceLimits(db);
        }
    }

    public List<JSONObject> peek(int max) throws Exception {
        synchronized (lock) {
            List<JSONObject> out = new ArrayList<>();
            if (max <= 0) return out;

            SQLiteDatabase db = helper.getReadableDatabase();
            String sql = "SELECT payload FROM " + TABLE_EVENTS + " ORDER BY id ASC LIMIT " + max;
            try (Cursor c = db.rawQuery(sql, null)) {
                while (c.moveToNext()) {
                    String json = c.getString(0);
                    if (json == null) continue;
                    out.add(new JSONObject(json));
                }
            }
            return out;
        }
    }

    public void drop(int n) throws Exception {
        synchronized (lock) {
            if (n <= 0) return;
            SQLiteDatabase db = helper.getWritableDatabase();
            String sql = "DELETE FROM " + TABLE_EVENTS +
                    " WHERE id IN (SELECT id FROM " + TABLE_EVENTS + " ORDER BY id ASC LIMIT " + n + ")";
            db.execSQL(sql);
        }
    }

    public int sizeEstimate() {
        synchronized (lock) {
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EVENTS, null)) {
                if (c.moveToFirst()) return c.getInt(0);
            } catch (Exception ignored) {}
            return 0;
        }
    }

    // Package-private for tests/diagnostics
    long byteSizeEstimate() {
        synchronized (lock) {
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor c = db.rawQuery("SELECT IFNULL(SUM(LENGTH(payload)),0) FROM " + TABLE_EVENTS, null)) {
                if (c.moveToFirst()) return c.getLong(0);
            } catch (Exception ignored) {}
            return 0;
        }
    }

    private void enforceLimits(SQLiteDatabase db) {
        long count = 0;
        long bytes = 0;
        try (Cursor c = db.rawQuery("SELECT COUNT(*), IFNULL(SUM(LENGTH(payload)),0) FROM " + TABLE_EVENTS, null)) {
            if (c.moveToFirst()) {
                count = c.getLong(0);
                bytes = c.getLong(1);
            }
        }

        if (count <= MAX_EVENTS && bytes <= MAX_BYTES) return;

        List<Long> idsToDelete = new ArrayList<>();
        long countAfter = count;
        long bytesAfter = bytes;

        try (Cursor c = db.rawQuery(
                "SELECT id, LENGTH(payload) AS len FROM " + TABLE_EVENTS + " ORDER BY id ASC",
                null)) {
            while (c.moveToNext() && (countAfter > MAX_EVENTS || bytesAfter > MAX_BYTES)) {
                long id = c.getLong(0);
                long len = c.getLong(1);
                idsToDelete.add(id);
                countAfter--;
                bytesAfter -= len;
            }
        }

        if (!idsToDelete.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < idsToDelete.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(idsToDelete.get(i));
            }
            db.execSQL("DELETE FROM " + TABLE_EVENTS + " WHERE id IN (" + sb + ")");

            Log.d(LOG_TAG, "purge count=" + idsToDelete.size() +
                    " sizeBefore=" + bytes + " sizeAfter=" + bytesAfter +
                    " countBefore=" + count + " countAfter=" + countAfter);
        }
    }

    // Visible for tests
    static int planPurgeCount(List<Long> lengths) {
        long count = lengths.size();
        long bytes = 0;
        for (Long l : lengths) bytes += l;

        int purged = 0;
        int i = 0;
        while ((count > MAX_EVENTS || bytes > MAX_BYTES) && i < lengths.size()) {
            bytes -= lengths.get(i);
            count--;
            purged++;
            i++;
        }
        return purged;
    }

    /**
     * Legacy migration: if an old JSONL file exists, import its events once into SQLite
     * to avoid losing queued reports on upgrade.
     */
    private void migrateFromLegacyFileIfPresent() {
        File legacy = new File(context.getFilesDir(), "foxtelemetry-queue.jsonl");
        if (!legacy.exists()) return;

        synchronized (lock) {
            try (BufferedReader br = new BufferedReader(new FileReader(legacy))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    enqueue(new JSONObject(line));
                }
                // delete after successful import
                //noinspection ResultOfMethodCallIgnored
                legacy.delete();
            } catch (Exception ignored) {
            }
        }
    }

    /** Simple SQLite helper for the queue table. */
    private static final class DB extends SQLiteOpenHelper {
        DB(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "payload TEXT NOT NULL)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Future migrations can go here; for now, a simple recreate is safe.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
            onCreate(db);
        }
    }
}
