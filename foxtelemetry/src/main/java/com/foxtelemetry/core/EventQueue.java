package com.foxtelemetry.core;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
