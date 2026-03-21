package com.foxtelemetry.collectors.threading;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.AnomalyCategory;
import com.foxtelemetry.model.Severity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight watchdog that posts a probe onto the main thread and measures how
 * long it takes to execute. One probe is in flight at a time to keep overhead low.
 */
public final class MainThreadMonitor {
    public interface AnomalyReporter {
        void report(@NonNull Anomaly anomaly);
    }

    public interface SessionIdProvider {
        @Nullable String getCurrentSessionId();
    }

    interface MainThreadPoster {
        void post(@NonNull Runnable runnable);
    }

    interface Cancellable {
        void cancel();
    }

    interface Scheduler {
        @NonNull
        Cancellable scheduleAtFixedRate(@NonNull Runnable runnable, long initialDelayMs, long periodMs);
    }

    interface Clock {
        long nowMs();
    }

    private final long slowThresholdMs;
    private final long blockedThresholdMs;
    private final long checkIntervalMs;
    @NonNull private final AnomalyReporter anomalyReporter;
    @Nullable private final SessionIdProvider sessionIdProvider;
    @NonNull private final MainThreadPoster mainThreadPoster;
    @NonNull private final Scheduler scheduler;
    @NonNull private final Clock clock;

    @Nullable private Probe currentProbe;
    @Nullable private Cancellable scheduledTask;
    private boolean started;

    public MainThreadMonitor(long slowThresholdMs,
                             long blockedThresholdMs,
                             @NonNull AnomalyReporter anomalyReporter,
                             @Nullable SessionIdProvider sessionIdProvider) {
        this(
                slowThresholdMs,
                blockedThresholdMs,
                anomalyReporter,
                sessionIdProvider,
                createAndroidPoster(),
                createExecutorScheduler(),
                createSystemClock()
        );
    }

    MainThreadMonitor(long slowThresholdMs,
                      long blockedThresholdMs,
                      @NonNull AnomalyReporter anomalyReporter,
                      @Nullable SessionIdProvider sessionIdProvider,
                      @NonNull MainThreadPoster mainThreadPoster,
                      @NonNull Scheduler scheduler,
                      @NonNull Clock clock) {
        this.slowThresholdMs = Math.max(100L, slowThresholdMs);
        this.blockedThresholdMs = Math.max(this.slowThresholdMs + 100L, blockedThresholdMs);
        this.checkIntervalMs = Math.min(500L, Math.max(100L, this.slowThresholdMs / 2L));
        this.anomalyReporter = anomalyReporter;
        this.sessionIdProvider = sessionIdProvider;
        this.mainThreadPoster = mainThreadPoster;
        this.scheduler = scheduler;
        this.clock = clock;
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        scheduledTask = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, 0L, checkIntervalMs);
    }

    public synchronized void stop() {
        started = false;
        currentProbe = null;
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
    }

    synchronized void tick() {
        if (!started) {
            return;
        }

        long nowMs = clock.nowMs();
        if (currentProbe == null) {
            final Probe probe = new Probe(nowMs);
            currentProbe = probe;
            mainThreadPoster.post(new Runnable() {
                @Override
                public void run() {
                    onProbeExecuted(probe);
                }
            });
            return;
        }

        evaluateProbe(currentProbe, nowMs);
    }

    synchronized void onProbeExecuted(@NonNull Probe probe) {
        if (!started || currentProbe != probe) {
            return;
        }

        evaluateProbe(probe, clock.nowMs());
        currentProbe = null;
    }

    private void evaluateProbe(@NonNull Probe probe, long nowMs) {
        long durationMs = Math.max(0L, nowMs - probe.postedAtMs);

        if (!probe.slowEmitted && durationMs >= slowThresholdMs) {
            probe.slowEmitted = true;
            anomalyReporter.report(createAnomaly(
                    "MAIN_THREAD_SLOW",
                    "Main thread slowdown detected",
                    Severity.WARNING,
                    "Main thread execution exceeded the slow threshold",
                    durationMs,
                    slowThresholdMs
            ));
        }

        if (!probe.blockedEmitted && durationMs >= blockedThresholdMs) {
            probe.blockedEmitted = true;
            anomalyReporter.report(createAnomaly(
                    "MAIN_THREAD_BLOCKED",
                    "Main thread blocked detected",
                    Severity.ERROR,
                    "Main thread appears blocked beyond the blocked threshold",
                    durationMs,
                    blockedThresholdMs
            ));
        }
    }

    @NonNull
    private Anomaly createAnomaly(@NonNull String code,
                                  @NonNull String title,
                                  @NonNull Severity severity,
                                  @NonNull String message,
                                  long durationMs,
                                  long thresholdMs) {
        Anomaly anomaly = new Anomaly(code, title, severity, AnomalyCategory.THREADING, message);
        anomaly.context.put("durationMs", durationMs);
        anomaly.context.put("thresholdMs", thresholdMs);
        anomaly.context.put("thresholdCrossed", code);
        String sessionId = sessionIdProvider != null ? sessionIdProvider.getCurrentSessionId() : null;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            anomaly.sessionId = sessionId;
            anomaly.context.put("sessionId", sessionId);
        }
        return anomaly;
    }

    @NonNull
    private static MainThreadPoster createAndroidPoster() {
        final Handler handler = new Handler(Looper.getMainLooper());
        return new MainThreadPoster() {
            @Override
            public void post(@NonNull Runnable runnable) {
                handler.post(runnable);
            }
        };
    }

    @NonNull
    private static Scheduler createExecutorScheduler() {
        return new Scheduler() {
            @NonNull
            @Override
            public Cancellable scheduleAtFixedRate(@NonNull Runnable runnable, long initialDelayMs, long periodMs) {
                final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                    @Override
                    public Thread newThread(@NonNull Runnable runnable) {
                        Thread thread = new Thread(runnable, "FoxTelemetryMainThreadMonitor");
                        thread.setDaemon(true);
                        return thread;
                    }
                });
                final java.util.concurrent.ScheduledFuture<?> future = executor.scheduleAtFixedRate(
                        runnable,
                        initialDelayMs,
                        periodMs,
                        TimeUnit.MILLISECONDS
                );
                return new Cancellable() {
                    @Override
                    public void cancel() {
                        future.cancel(true);
                        executor.shutdownNow();
                    }
                };
            }
        };
    }

    @NonNull
    private static Clock createSystemClock() {
        return new Clock() {
            @Override
            public long nowMs() {
                return System.currentTimeMillis();
            }
        };
    }

    static final class Probe {
        final long postedAtMs;
        boolean slowEmitted;
        boolean blockedEmitted;

        Probe(long postedAtMs) {
            this.postedAtMs = postedAtMs;
        }
    }
}
