# FoxTelemetry (Android) v1.0.3

Android Java telemetry SDK (crashes + caught errors + logs), auto-initialized like Firebase via a `ContentProvider`.

## Features
- Automatic startup via ContentProvider (no code needed when JSON config is present)
- Crash + caught-exception reporting, structured log capture
- Offline-friendly: events stored locally in SQLite and retried when network/API is back
- Stable installId generated once per install (persisted locally, never logged)

## 1) Add the JSON config (like google-services.json)

Create this file in your app:
`app/src/main/assets/foxtelemetry.json`

Example:
```json
{
  "foxTelemetry": {
    "projectId": "proj_xxx",
    "appId": "app_xxx",
    "packageName": "com.example.myapp",
    "endpoint": "https://api.yourdomain.com/v1/ingest",
    "ingestKey": "fx_live_xxx",
    "environment": "production"
  }
}
```

## 2) Add dependency (JitPack)

```gradle
repositories {
  maven { url "https://jitpack.io" }
}

dependencies {
  implementation "com.github.foxhosdev:FoxTelemetry:v1.0.3"
}
```

## 3) Use it

Auto init happens at app startup (no `init()` required) if `foxtelemetry.json` exists.

```java
FoxTelemetry.i("APP", "Started");

try {
    int x = 5 / 0;
} catch (Exception e) {
    FoxTelemetry.report(e, "DIVISION_TEST");
}
```

### Optional: manual init (if you prefer code-based config)
```java
FoxTelemetryConfig cfg = new FoxTelemetryConfig(
    "proj_xxx", "app_xxx", "com.example.myapp",
    "https://api.yourdomain.com/v1/ingest", "fx_live_xxx",
    "production", /* enableCrashCapture */ true
);
FoxTelemetry.init(appContext, cfg);
```

## Notes

- Release builds with R8/ProGuard may obfuscate stack traces. For correct file/line in dashboard, upload `mapping.txt` per version and deobfuscate server-side.
- Library is built with JDK 17 / AGP 8.2; ensure your project toolchain matches or sets `JAVA_HOME` to 17 when building.
- Events are persisted locally (SQLite) and retried automatically when the API is unreachable.
- Queued data flushes through `WorkManager`; you can force it with `FoxTelemetry.flushAsync(context)`.
- installId: created once on first init and persisted; survives app restarts but is not logged or transmitted outside telemetry payloads.
