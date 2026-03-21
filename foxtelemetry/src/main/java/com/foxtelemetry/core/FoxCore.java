package com.foxtelemetry.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.api.FoxTelemetryConfig;
import com.foxtelemetry.api.Trace;
import com.foxtelemetry.collectors.threading.MainThreadMonitor;
import com.foxtelemetry.intelligence.CorrelationEngine;
import com.foxtelemetry.intelligence.EventCorrelationWindow;
import com.foxtelemetry.intelligence.Insight;
import com.foxtelemetry.intelligence.InsightEngine;
import com.foxtelemetry.intelligence.SignalNoiseController;
import com.foxtelemetry.intelligence.scoring.QualityScore;
import com.foxtelemetry.intelligence.scoring.QualityScoreCalculator;
import com.foxtelemetry.intelligence.rules.CallbackStormRule;
import com.foxtelemetry.intelligence.rules.RepeatedExceptionRule;
import com.foxtelemetry.intelligence.rules.Rule;
import com.foxtelemetry.intelligence.rules.RuleEngine;
import com.foxtelemetry.intelligence.rules.SlowTraceRule;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.AnomalyEvent;
import com.foxtelemetry.model.BaseEvent;
import com.foxtelemetry.model.ErrorEvent;
import com.foxtelemetry.model.LogEvent;
import com.foxtelemetry.model.TrackEvent;
import com.foxtelemetry.storage.EventStore;
import com.foxtelemetry.storage.SQLiteEventStore;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Internal engine for FoxTelemetry v2.
 */
public final class FoxCore implements TelemetryEngine {
    private static final String TAG = "FoxTelemetryCore";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private static volatile FoxCore instance;

    private final Context context;
    private volatile FoxTelemetryConfig config;
    private final EventStore eventStore;
    private final String installId;
    private volatile String sessionId;
    private volatile boolean lifecycleCallbacksRegistered;
    private final com.foxtelemetry.collectors.session.SessionManager sessionManager;
    private final com.foxtelemetry.collectors.breadcrumb.BreadcrumbManager breadcrumbManager;
    private final com.foxtelemetry.collectors.anomaly.AnomalyManager anomalyManager;
    private volatile CorrelationEngine correlationEngine;
    private volatile RuleEngine ruleEngine;
    private volatile InsightEngine insightEngine;
    private final QualityScoreCalculator qualityScoreCalculator;
    private volatile SignalNoiseController signalNoiseController;
    private volatile MainThreadMonitor mainThreadMonitor;
    private final ThreadLocal<Deque<String>> activeTraceStack = new ThreadLocal<>();

    private FoxCore(@NonNull Context context, @NonNull FoxTelemetryConfig config) {
        this.context = context.getApplicationContext();
        this.config = config;
        this.eventStore = new SQLiteEventStore(this.context);
        this.installId = com.foxtelemetry.core.InstallIdStore.getOrCreateInstallId(this.context);
        this.sessionManager = new com.foxtelemetry.collectors.session.SessionManager(config.sessionTimeoutMs);
        this.breadcrumbManager = new com.foxtelemetry.collectors.breadcrumb.BreadcrumbManager(config.maxBreadcrumbs);
        this.anomalyManager = new com.foxtelemetry.collectors.anomaly.AnomalyManager();
        this.correlationEngine = new CorrelationEngine(config.maxCorrelationEvents, config.correlationWindowMs);
        this.ruleEngine = new RuleEngine(buildRules(config));
        this.insightEngine = new InsightEngine(config.intelligenceCooldownMs);
        this.qualityScoreCalculator = new QualityScoreCalculator();
        this.signalNoiseController = new SignalNoiseController(
                config.signalNoiseWindowMs,
                config.maxDuplicateLogsPerWindow,
                config.logSampleRateAfterLimit,
                config.maxDuplicateTracksPerWindow,
                config.trackSampleRateAfterLimit
        );
        registerLifecycleCallbacksIfNeeded();
        configureMainThreadMonitor(config);
    }

    public static void init(@NonNull Context context, @NonNull FoxTelemetryConfig config) {
        synchronized (FoxCore.class) {
            if (initialized.compareAndSet(false, true) || instance == null) {
                instance = new FoxCore(context, config);
                Log.i(TAG, "FoxTelemetry initialized");
                instance.addBreadcrumb("app", "FoxTelemetry initialized");
            } else {
                instance.updateConfig(config);
                Log.i(TAG, "FoxTelemetry config updated");
            }

            if (config.enableCrashCapture) {
                Thread.UncaughtExceptionHandler current = Thread.getDefaultUncaughtExceptionHandler();
                if (!(current instanceof CrashHandler)) {
                    Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(current));
                }
            }
        }
    }

    @Nullable
    public static FoxCore getInstance() {
        return instance;
    }

    @Override
    public boolean isInitialized() {
        return initialized.get() && instance != null;
    }

    @Override
    public void dispatch(@NonNull BaseEvent event) {
        try {
            dispatchInternal(event, true);
        } catch (Exception e) {
            Log.e(TAG, "Failed to dispatch event", e);
        }
    }

    private String getAppVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ignored) {
            return "unknown";
        }
    }

    private void triggerFlush() {
        com.foxtelemetry.FoxTelemetry.flushAsync(context);
    }

    private void updateConfig(@NonNull FoxTelemetryConfig newConfig) {
        this.config = newConfig;
        this.correlationEngine = new CorrelationEngine(newConfig.maxCorrelationEvents, newConfig.correlationWindowMs);
        this.ruleEngine = new RuleEngine(buildRules(newConfig));
        this.insightEngine = new InsightEngine(newConfig.intelligenceCooldownMs);
        this.signalNoiseController = new SignalNoiseController(
                newConfig.signalNoiseWindowMs,
                newConfig.maxDuplicateLogsPerWindow,
                newConfig.logSampleRateAfterLimit,
                newConfig.maxDuplicateTracksPerWindow,
                newConfig.trackSampleRateAfterLimit
        );
        registerLifecycleCallbacksIfNeeded();
        configureMainThreadMonitor(newConfig);
    }

    private void dispatchInternal(@NonNull BaseEvent event, boolean analyze) throws Exception {
        enrichEvent(event);

        if (!shouldPersist(event)) {
            return;
        }

        List<Anomaly> derivedAnomalies = Collections.emptyList();
        EventCorrelationWindow window = null;
        if (config.enableRuntimeIntelligence && correlationEngine != null) {
            window = correlationEngine.record(event);
            if (analyze && ruleEngine != null) {
                derivedAnomalies = ruleEngine.evaluate(event, window);
            }
        }

        eventStore.enqueue(event);

        if (event instanceof AnomalyEvent && window != null) {
            emitInsights(window);
            updateQualityScore(window);
        }

        for (Anomaly anomaly : derivedAnomalies) {
            emitDerivedAnomaly(anomaly);
        }

        triggerFlush();
    }

    private void emitDerivedAnomaly(@NonNull Anomaly anomaly) throws Exception {
        if (!config.enableAnomalyDetection) {
            return;
        }

        AnomalyEvent anomalyEvent = new AnomalyEvent(anomaly);
        enrichEvent(anomalyEvent);
        syncAnomalyIdentity(anomalyEvent);
        if (!anomalyManager.shouldReport(anomalyEvent.anomaly, anomalyEvent.sessionId, anomalyEvent.installId)) {
            return;
        }

        EventCorrelationWindow window = null;
        if (config.enableRuntimeIntelligence && correlationEngine != null) {
            window = correlationEngine.record(anomalyEvent);
        }
        eventStore.enqueue(anomalyEvent);
        if (window != null) {
            emitInsights(window);
            updateQualityScore(window);
        }
    }

    private void enrichEvent(@NonNull BaseEvent event) {
        event.sessionId = config.enableSessionTracking ? sessionManager.getCurrentSessionId() : null;
        event.installId = installId;
        event.userId = config.userId;
        event.appVersion = getAppVersion();
        event.packageName = config.packageName;
        event.environment = config.environment;
        event.screenName = sessionManager.getCurrentScreenName();
        event.activeTraceName = getCurrentActiveTraceName();
        event.networkState = getNetworkState();

        if (config.enableAutoBreadcrumbs && !(event instanceof LogEvent)) {
            event.breadcrumbs = new org.json.JSONArray(breadcrumbManager.getBreadcrumbsAsJson());
        }
    }

    private void syncAnomalyIdentity(@NonNull AnomalyEvent event) {
        event.syncAnomalyIdentity();
    }

    private void emitInsights(@NonNull EventCorrelationWindow window) throws Exception {
        InsightEngine engine = insightEngine;
        if (engine == null) {
            return;
        }
        List<Insight> insights = engine.evaluate(window.getSessionKey(), window.getAnomalies(), System.currentTimeMillis());
        for (Insight insight : insights) {
            enrichEvent(insight);
            eventStore.enqueue(insight);
            if (config.enableDebugLogs) {
                Log.i(TAG, "Insight detected: " + insight.title + " (" + insight.type + ")");
            }
        }
    }

    private void updateQualityScore(@NonNull EventCorrelationWindow window) {
        QualityScore score = qualityScoreCalculator.calculate(window.getSessionKey(), window.getAnomalies());
        if (config.enableDebugLogs) {
            Log.i(
                    TAG,
                    "Quality score session=" + score.sessionId
                            + " reliability=" + score.reliabilityScore
                            + " performance=" + score.performanceScore
                            + " overall=" + score.overallScore
                            + " penalties=" + score.appliedPenaltyCodes
            );
        }
    }

    private boolean shouldPersist(@NonNull BaseEvent event) {
        SignalNoiseController controller = signalNoiseController;
        return controller == null || controller.shouldStore(event);
    }

    @NonNull
    private List<Rule> buildRules(@NonNull FoxTelemetryConfig config) {
        List<Rule> rules = new ArrayList<>();
        rules.add(new RepeatedExceptionRule(
                config.repeatedExceptionThreshold,
                config.repeatedExceptionWindowMs,
                config.intelligenceCooldownMs
        ));
        rules.add(new CallbackStormRule(
                config.callbackStormThreshold,
                config.callbackStormWindowMs,
                config.intelligenceCooldownMs
        ));
        rules.add(new SlowTraceRule(
                config.slowTraceThresholdMs,
                config.intelligenceCooldownMs
        ));
        return rules;
    }

    private synchronized void configureMainThreadMonitor(@NonNull FoxTelemetryConfig config) {
        if (mainThreadMonitor != null) {
            mainThreadMonitor.stop();
            mainThreadMonitor = null;
        }

        if (!config.enableMainThreadMonitor || !config.enableAnomalyDetection) {
            return;
        }

        mainThreadMonitor = new MainThreadMonitor(
                config.mainThreadSlowThresholdMs,
                config.mainThreadBlockedThresholdMs,
                new MainThreadMonitor.AnomalyReporter() {
                    @Override
                    public void report(@NonNull Anomaly anomaly) {
                        captureAnomaly(anomaly);
                    }
                },
                new MainThreadMonitor.SessionIdProvider() {
                    @Nullable
                    @Override
                    public String getCurrentSessionId() {
                        return config.enableSessionTracking ? sessionManager.getCurrentSessionId() : null;
                    }
                }
        );
        mainThreadMonitor.start();
    }

    private void registerLifecycleCallbacksIfNeeded() {
        if (lifecycleCallbacksRegistered) {
            return;
        }
        if (!(context instanceof android.app.Application)) {
            return;
        }
        if (!config.enableSessionTracking && !config.enableAutoBreadcrumbs) {
            return;
        }
        ((android.app.Application) context).registerActivityLifecycleCallbacks(sessionManager);
        lifecycleCallbacksRegistered = true;
    }

    @Override
    @NonNull
    public FoxTelemetryConfig getConfig() {
        return config;
    }

    @Override
    public void setUserId(@Nullable String userId) {
        this.config = config.withUserId(userId);
    }

    @NonNull
    public EventStore getEventStore() {
        return eventStore;
    }

    @NonNull
    public String getInstallId() {
        return installId;
    }

    @Override
    public void addBreadcrumb(@NonNull String category, @NonNull String message) {
        if (!config.enableAutoBreadcrumbs) {
            return;
        }
        breadcrumbManager.addBreadcrumb(category, message);
    }

    @Nullable
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(@Nullable String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void report(@NonNull Throwable throwable, @Nullable String contextTag, boolean isFatal) {
        dispatch(new ErrorEvent(throwable, contextTag, isFatal));
    }

    @Override
    public void track(@NonNull String name, @Nullable Map<String, String> attributes) {
        dispatch(new TrackEvent(name, attributes));
    }

    @Override
    public void captureAnomaly(@NonNull com.foxtelemetry.model.Anomaly anomaly) {
        if (!config.enableAnomalyDetection) {
            return;
        }
        anomaly.sessionId = config.enableSessionTracking ? sessionManager.getCurrentSessionId() : null;
        anomaly.installId = installId;
        if (anomalyManager.shouldReport(anomaly, anomaly.sessionId, anomaly.installId)) {
            dispatch(new AnomalyEvent(anomaly));
        }
    }

    @Override
    @Nullable
    public Trace startTrace(@NonNull String name) {
        return new ManagedTrace(this, name);
    }

    @Override
    public void flush() {
        triggerFlush();
    }

    void onTraceStarted(@NonNull String traceName) {
        Deque<String> stack = activeTraceStack.get();
        if (stack == null) {
            stack = new ArrayDeque<>();
            activeTraceStack.set(stack);
        }
        stack.push(traceName);
    }

    void onTraceEnded() {
        Deque<String> stack = activeTraceStack.get();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        stack.pop();
        if (stack.isEmpty()) {
            activeTraceStack.remove();
        }
    }

    @Nullable
    private String getCurrentActiveTraceName() {
        Deque<String> stack = activeTraceStack.get();
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }

    @NonNull
    private String getNetworkState() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return "unknown";
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                //noinspection deprecation
                android.net.NetworkInfo info = cm.getActiveNetworkInfo();
                if (info == null || !info.isConnected()) {
                    return "offline";
                }
                int type = info.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    return "wifi";
                }
                if (type == ConnectivityManager.TYPE_MOBILE) {
                    return "cellular";
                }
                if (type == ConnectivityManager.TYPE_ETHERNET) {
                    return "ethernet";
                }
                return "connected";
            }

            Network network = cm.getActiveNetwork();
            if (network == null) {
                return "offline";
            }

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities == null) {
                return "connected";
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return "wifi";
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return "cellular";
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return "ethernet";
            }
            return "connected";
        } catch (Throwable ignored) {
            return "unknown";
        }
    }
}
