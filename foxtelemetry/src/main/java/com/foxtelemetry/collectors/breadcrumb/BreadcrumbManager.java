package com.foxtelemetry.collectors.breadcrumb;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages breadcrumbs for the current session.
 */
public final class BreadcrumbManager {

    private final int maxBreadcrumbs;
    private final LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();

    public BreadcrumbManager(int maxBreadcrumbs) {
        this.maxBreadcrumbs = maxBreadcrumbs;
    }

    public synchronized void addBreadcrumb(@NonNull String category, @NonNull String message) {
        if (breadcrumbs.size() >= maxBreadcrumbs) {
            breadcrumbs.removeFirst();
        }
        breadcrumbs.add(new Breadcrumb(category, message));
    }

    public synchronized List<JSONObject> getBreadcrumbsAsJson() {
        List<JSONObject> list = new LinkedList<>();
        for (Breadcrumb b : breadcrumbs) {
            try {
                list.add(b.toJson());
            } catch (JSONException ignored) {}
        }
        return list;
    }

    private static class Breadcrumb {
        final String category;
        final String message;
        final long timestamp;

        Breadcrumb(String category, String message) {
            this.category = category;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("category", category);
            json.put("message", message);
            json.put("timestamp", timestamp);
            return json;
        }
    }
}
