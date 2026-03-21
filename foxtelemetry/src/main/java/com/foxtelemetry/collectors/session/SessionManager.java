package com.foxtelemetry.collectors.session;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.core.FoxCore;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages app sessions and tracks foreground/background states.
 */
public final class SessionManager implements Application.ActivityLifecycleCallbacks {
    private final long sessionTimeoutMs;
    private final AtomicInteger activeActivities = new AtomicInteger(0);

    private String currentSessionId;
    private String currentScreenName;
    private long lastBackgroundTime = -1;

    public SessionManager(long sessionTimeoutMs) {
        this.sessionTimeoutMs = Math.max(1_000L, sessionTimeoutMs);
        startNewSession();
    }

    public synchronized String getCurrentSessionId() {
        return currentSessionId;
    }

    @Nullable
    public synchronized String getCurrentScreenName() {
        return currentScreenName;
    }

    private void startNewSession() {
        currentSessionId = UUID.randomUUID().toString();
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.setSessionId(currentSessionId);
            core.addBreadcrumb("session", "New session started: " + currentSessionId);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.addBreadcrumb("activity", "Activity started: " + activity.getClass().getSimpleName());
        }
        if (activeActivities.getAndIncrement() == 0) {
            onAppForeground();
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.addBreadcrumb("activity", "Activity stopped: " + activity.getClass().getSimpleName());
        }
        synchronized (this) {
            if (activity.getClass().getSimpleName().equals(currentScreenName)) {
                currentScreenName = null;
            }
        }
        if (activeActivities.decrementAndGet() == 0) {
            onAppBackground();
        }
    }

    private void onAppForeground() {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.addBreadcrumb("app", "App foregrounded");
        }
        long now = System.currentTimeMillis();
        if (lastBackgroundTime != -1 && (now - lastBackgroundTime) > sessionTimeoutMs) {
            startNewSession();
        }
        lastBackgroundTime = -1;
    }

    private void onAppBackground() {
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.addBreadcrumb("app", "App backgrounded");
        }
        lastBackgroundTime = System.currentTimeMillis();
    }

    // Other lifecycle methods (unused for now)
    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        synchronized (this) {
            currentScreenName = activity.getClass().getSimpleName();
        }
        FoxCore core = FoxCore.getInstance();
        if (core != null) {
            core.addBreadcrumb("activity", "Activity resumed: " + activity.getClass().getSimpleName());
        }
    }
    @Override public void onActivityPaused(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) {}
}
