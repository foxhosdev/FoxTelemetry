package com.foxtelemetry.collectors.threading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.foxtelemetry.model.Anomaly;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MainThreadMonitorTest {

    @Test
    public void emitsMainThreadSlowWhenProbeExceedsSlowThreshold() {
        FakeClock clock = new FakeClock();
        FakePoster poster = new FakePoster();
        FakeScheduler scheduler = new FakeScheduler();
        CollectingReporter reporter = new CollectingReporter();

        MainThreadMonitor monitor = new MainThreadMonitor(
                400L,
                3_000L,
                reporter,
                new FixedSessionProvider("session-1"),
                poster,
                scheduler,
                clock
        );

        monitor.start();
        scheduler.runTick();
        clock.advance(450L);
        scheduler.runTick();

        assertEquals(1, reporter.anomalies.size());
        Anomaly anomaly = reporter.anomalies.get(0);
        assertEquals("MAIN_THREAD_SLOW", anomaly.code);
        assertEquals("session-1", anomaly.context.get("sessionId"));
        assertEquals(400L, anomaly.context.get("thresholdMs"));
    }

    @Test
    public void emitsMainThreadBlockedWhenProbeExceedsBlockedThreshold() {
        FakeClock clock = new FakeClock();
        FakePoster poster = new FakePoster();
        FakeScheduler scheduler = new FakeScheduler();
        CollectingReporter reporter = new CollectingReporter();

        MainThreadMonitor monitor = new MainThreadMonitor(
                400L,
                3_000L,
                reporter,
                new FixedSessionProvider("session-2"),
                poster,
                scheduler,
                clock
        );

        monitor.start();
        scheduler.runTick();
        clock.advance(3_100L);
        scheduler.runTick();

        assertEquals(2, reporter.anomalies.size());
        assertEquals("MAIN_THREAD_SLOW", reporter.anomalies.get(0).code);
        assertEquals("MAIN_THREAD_BLOCKED", reporter.anomalies.get(1).code);
        assertEquals(3_000L, reporter.anomalies.get(1).context.get("thresholdMs"));
    }

    @Test
    public void doesNotEmitWhenProbeCompletesBeforeThreshold() {
        FakeClock clock = new FakeClock();
        FakePoster poster = new FakePoster();
        FakeScheduler scheduler = new FakeScheduler();
        CollectingReporter reporter = new CollectingReporter();

        MainThreadMonitor monitor = new MainThreadMonitor(
                400L,
                3_000L,
                reporter,
                new FixedSessionProvider("session-3"),
                poster,
                scheduler,
                clock
        );

        monitor.start();
        scheduler.runTick();
        clock.advance(50L);
        poster.runPosted();

        assertTrue(reporter.anomalies.isEmpty());
    }

    private static final class FakeClock implements MainThreadMonitor.Clock {
        private long nowMs;

        @Override
        public long nowMs() {
            return nowMs;
        }

        void advance(long deltaMs) {
            nowMs += deltaMs;
        }
    }

    private static final class FakePoster implements MainThreadMonitor.MainThreadPoster {
        private Runnable posted;

        @Override
        public void post(Runnable runnable) {
            this.posted = runnable;
        }

        void runPosted() {
            if (posted != null) {
                Runnable runnable = posted;
                posted = null;
                runnable.run();
            }
        }
    }

    private static final class FakeScheduler implements MainThreadMonitor.Scheduler {
        private Runnable task;

        @Override
        public MainThreadMonitor.Cancellable scheduleAtFixedRate(Runnable runnable, long initialDelayMs, long periodMs) {
            this.task = runnable;
            return new MainThreadMonitor.Cancellable() {
                @Override
                public void cancel() {
                    task = null;
                }
            };
        }

        void runTick() {
            if (task != null) {
                task.run();
            }
        }
    }

    private static final class CollectingReporter implements MainThreadMonitor.AnomalyReporter {
        private final List<Anomaly> anomalies = new ArrayList<>();

        @Override
        public void report(Anomaly anomaly) {
            anomalies.add(anomaly);
        }
    }

    private static final class FixedSessionProvider implements MainThreadMonitor.SessionIdProvider {
        private final String sessionId;

        private FixedSessionProvider(String sessionId) {
            this.sessionId = sessionId;
        }

        @Override
        public String getCurrentSessionId() {
            return sessionId;
        }
    }
}
