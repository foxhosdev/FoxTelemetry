# FoxTelemetry (Android) v1.0.1

Android Java telemetry SDK (crashes + caught errors + logs), auto-initialized like Firebase via a `ContentProvider`.

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
  implementation "com.github.foxhosdev.FoxTelemetry:foxtelemetry:v1.0.1"
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

## Notes

- Release builds with R8/ProGuard may obfuscate stack traces. For correct file/line in dashboard, upload `mapping.txt` per version and deobfuscate server-side.
- Library is built with JDK 17 / AGP 8.2; ensure your project toolchain matches or sets `JAVA_HOME` to 17 when building.
