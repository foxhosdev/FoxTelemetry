![FoxTelemetry Logo](./logo.png)

# FoxTelemetry
## Runtime Code Quality & Software Quality SDK for Android

FoxTelemetry is a professional Android Java SDK focused on runtime code quality, software quality monitoring, anomaly detection, and intelligent diagnostics.

## Introduction

FoxTelemetry is not a simple logging SDK.

It is designed to detect runtime anomalies, analyze how code behaves in production, and produce actionable diagnostics that help teams understand software quality degradation before issues become user-visible failures.

Instead of only collecting raw logs and crashes, FoxTelemetry helps answer practical questions:

- Is the code behaving abnormally at runtime?
- Is this issue repeated or isolated?
- Is performance degrading?
- Is a user flow unstable?
- What is the most probable cause?

## Key Features

- Automatic anomaly detection
- Rule-based runtime analysis
- Insight generation for diagnostics
- Quality scoring per session
- Lightweight and Android-friendly runtime design
- Easy integration with a clean public API

## Why FoxTelemetry?

Traditional mobile observability tools usually focus on:

- logs
- crashes

FoxTelemetry goes further:

- detects bad runtime behavior automatically
- identifies unstable flows and repeated failures
- highlights performance issues and callback storms
- reduces noise by prioritizing meaningful quality signals
- produces insights, not just raw data

The goal is not to say "here are your logs".

The goal is to say "here are the real runtime quality issues, their likely cause, and their likely impact".

## Architecture Overview

FoxTelemetry keeps a simple internal pipeline:

`Public API -> FoxCore -> Correlation Engine -> Anomaly Engine -> Insight Engine -> Storage (SQLite) -> Worker -> Network`

At a high level:

- `Public API` keeps developer integration simple
- `FoxCore` orchestrates enrichment, storage, and runtime intelligence
- `Correlation Engine` groups related runtime signals by session and time window
- `Anomaly Engine` emits actionable anomaly events
- `Insight Engine` turns anomalies into diagnostics
- `SQLite` provides resilient offline storage
- `Worker` handles background flush and retries
- `Network` ships batched payloads to your backend

## Installation

Gradle:

```gradle
dependencies {
    implementation "com.foxtelemetry:foxtelemetry:1.2.0"
}
```

If you publish through a local Maven repository or your own package registry, keep the same semantic version.

## Quick Start

```java
import android.app.Application;

import com.foxtelemetry.api.FoxTelemetry;
import com.foxtelemetry.api.FoxTelemetryConfig;
import com.foxtelemetry.api.Trace;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.AnomalyCategory;
import com.foxtelemetry.model.Severity;

public final class DemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FoxTelemetryConfig config = new FoxTelemetryConfig.Builder()
                .projectId("demo-project")
                .appId("demo-android-app")
                .packageName(getPackageName())
                .endpoint("https://api.example.com/ingest")
                .ingestKey("demo_ingest_key")
                .environment("production")
                .enableAnomalyDetection(true)
                .enableRuntimeIntelligence(true)
                .enableDebugLogs(false)
                .build();

        FoxTelemetry.init(this, config);

        Trace trace = FoxTelemetry.startTrace("checkout");
        try {
            // your code
        } finally {
            if (trace != null) {
                trace.end("ok");
            }
        }

        FoxTelemetry.anomaly(new Anomaly(
                "CUSTOM_RUNTIME_WARNING",
                "Manual anomaly example",
                Severity.WARNING,
                AnomalyCategory.RELIABILITY,
                "A runtime condition requires investigation"
        ));
    }
}
```

## Example Output

Example anomalies:

- `REPEATED_EXCEPTION`
- `CALLBACK_STORM`
- `SLOW_TRACE`
- `MAIN_THREAD_SLOW`

Example insights:

- `performance_issue`
- `unstable_flow`
- `runtime_loop_issue`

Example quality score:

```text
performanceScore: 85
reliabilityScore: 90
overallScore: 87
```

## Configuration

FoxTelemetry is configurable through `FoxTelemetryConfig.Builder`.

Common configuration areas:

- Thresholds
  - `slowTraceThresholdMs`
  - `mainThreadSlowThresholdMs`
  - `mainThreadBlockedThresholdMs`
  - `repeatedExceptionThreshold`
  - `callbackStormThreshold`
- Debug mode
  - `enableDebugLogs`
- Feature toggles
  - `enableCrashCapture`
  - `enableAnomalyDetection`
  - `enableRuntimeIntelligence`
  - `enableMainThreadMonitor`
  - `enableAutoBreadcrumbs`
  - `enableSessionTracking`

Example:

```java
FoxTelemetryConfig config = new FoxTelemetryConfig.Builder()
        .projectId("demo-project")
        .appId("demo-android-app")
        .packageName("com.example.app")
        .endpoint("https://api.example.com/ingest")
        .ingestKey("demo_ingest_key")
        .slowTraceThresholdMs(2000L)
        .mainThreadSlowThresholdMs(400L)
        .mainThreadBlockedThresholdMs(3000L)
        .repeatedExceptionThreshold(5)
        .callbackStormThreshold(20)
        .enableDebugLogs(true)
        .build();
```

## Versioning

FoxTelemetry uses Semantic Versioning.

Current version: `1.2.0`

- `MAJOR`: breaking API or behavior changes
- `MINOR`: backward-compatible features and improvements
- `PATCH`: backward-compatible fixes and maintenance updates

## Roadmap

- Main thread monitor hardening and tuning
- Memory tracking
- Release health analysis

## Contributing

Contributions, issues, and improvement proposals are welcome. Please open an issue before large changes so architecture and release direction stay consistent.

## License

MIT License
