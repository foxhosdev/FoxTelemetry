package com.foxtelemetry.core;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * MVP queue: JSON Lines file in app private storage.
 * Each line is a JSON event.
 */
public final class EventQueue {

    private final Context context;
    private final File file;
    private final Object lock = new Object();

    public EventQueue(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.file = new File(this.context.getFilesDir(), "foxtelemetry-queue.jsonl");
    }

    public Context getContext() { return context; }

    public void enqueue(@NonNull JSONObject event) throws Exception {
        synchronized (lock) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                bw.write(event.toString());
                bw.newLine();
            }
        }
    }

    public List<JSONObject> peek(int max) throws Exception {
        synchronized (lock) {
            List<JSONObject> out = new ArrayList<>();
            if (!file.exists()) return out;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null && out.size() < max) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    out.add(new JSONObject(line));
                }
            }
            return out;
        }
    }

    public void drop(int n) throws Exception {
        synchronized (lock) {
            if (n <= 0 || !file.exists()) return;

            List<String> remaining = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int i = 0;
                while ((line = br.readLine()) != null) {
                    if (i++ < n) continue;
                    remaining.add(line);
                }
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                for (String l : remaining) {
                    bw.write(l);
                    bw.newLine();
                }
            }
        }
    }

    public int sizeEstimate() {
        synchronized (lock) {
            if (!file.exists()) return 0;
            int c = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (br.readLine() != null) c++;
            } catch (Exception ignored) {}
            return c;
        }
    }
}
