# FoxTelemetry Event Schema

## Purpose

This document describes the event payloads produced by the FoxTelemetry Android Java SDK before backend ingestion.

The goal is to keep the ingest contract stable enough for backend work without coupling the backend to Android source code.

## Contract layers

FoxTelemetry uses four version concepts:

- `schemaVersion`: version of the event payload contract
- `sdkVersion`: version of the runtime SDK producing the event
- package version: version of the published library artifact
- ingest API version: version of the HTTP ingest contract

For backend compatibility, `schemaVersion` is the primary contract key.

Current values in this repository:

- `schemaVersion`: `2.0`
- `sdkVersion`: `2.0.0`
- ingest API version: `v1`

## Root batch payload

FoxTelemetry sends batches with this root structure:

```json
{
  "projectId": "demo-project",
  "appId": "android-main",
  "packageName": "com.example.app",
  "batchId": "0d3d52f2d3f0c6f7c7e63f4f5d52d4c8812f19f93d7e3d36fbb31806aa7ce7b4",
  "sentAt": 1710000000000,
  "apiVersion": "v1",
  "eventCount": 2,
  "events": []
}
```

## Root batch fields

Required fields:

- `projectId`: backend project identifier
- `appId`: logical app identifier inside the project
- `packageName`: Android package name of the client app
- `batchId`: deterministic batch identifier derived from batch contents
- `sentAt`: client send timestamp in epoch milliseconds
- `apiVersion`: ingest contract version
- `eventCount`: number of events in `events`
- `events`: array of event payloads

Notes:

- `batchId` is stable for the same event set and order, which helps retries and backend auditing.
- `eventCount` must match `events.length`.

## Event envelope

Every event contains a common envelope plus type-specific fields.

### Required envelope fields

- `eventId`
- `timestamp`
- `type`
- `schemaVersion`

### Common optional metadata

- `sessionId`
- `installId`
- `userId`
- `appVersion`
- `versionCode`
- `buildType`
- `flavor`
- `releaseChannel`
- `buildId`
- `packageName`
- `environment`
- `screenName`
- `activeTraceName`
- `networkState`
- `sdkVersion`
- `breadcrumbs`
- `release`
- `device`

### Common nested objects

`release` object:

- `versionName`
- `versionCode`
- `buildType`
- `flavor`
- `releaseChannel`
- `buildId`

`device` object:

- `brand`
- `manufacturer`
- `model`
- `androidVersion`
- `sdkInt`

The flat release fields are kept for compatibility and indexing convenience.
The nested `release` and `device` objects are the clean structured representation.

## Event types

Current event types:

- `error`
- `log`
- `track`
- `trace`
- `anomaly`
- `insight`

## Error event

Required fields:

- all required envelope fields
- `exceptionName`
- `stackTrace`
- `contextTag`
- `isFatal`

Optional fields:

- all common optional metadata

## Log event

Required fields:

- all required envelope fields
- `level`
- `tag`
- `message`

Optional fields:

- all common optional metadata

## Track event

Required fields:

- all required envelope fields
- `name`
- `attributes`

Optional fields:

- all common optional metadata

## Trace event

Required fields:

- all required envelope fields
- `name`
- `durationMs`
- `status`
- `tags`

Optional fields:

- all common optional metadata

## Anomaly event

Required fields:

- all required envelope fields
- `code`
- `title`
- `severity`
- `category`
- `message`
- `fingerprint`
- `occurrenceCount`
- `context`

Optional fields:

- `anomalySessionId`
- `anomalyInstallId`
- all common optional metadata

## Insight event

Required fields:

- all required envelope fields
- `insightType`
- `title`
- `summary`
- `confidence`
- `relatedAnomalyCodes`

Optional fields:

- `probableCause`
- `impact`
- all common optional metadata

## schemaVersion and sdkVersion

`schemaVersion`:

- identifies the payload format expected by the backend
- should change only when the event contract changes incompatibly
- should remain stable for backward-compatible field additions

`sdkVersion`:

- identifies the runtime implementation that produced the event
- is useful for support, diagnostics, rollout tracking, and compatibility monitoring
- should not be used as the primary parsing key on the backend

## JSON example: anomaly event

```json
{
  "eventId": "evt_01",
  "timestamp": 1710000001000,
  "type": "anomaly",
  "schemaVersion": "2.0",
  "sessionId": "session_1",
  "installId": "install_1",
  "appVersion": "1.1.0",
  "versionCode": 42,
  "buildType": "release",
  "flavor": "play",
  "releaseChannel": "production",
  "buildId": "2026.03.21.1",
  "packageName": "com.example.app",
  "environment": "production",
  "screenName": "CheckoutActivity",
  "networkState": "wifi",
  "sdkVersion": "2.0.0",
  "code": "CALLBACK_STORM",
  "title": "Callback storm detected",
  "severity": "WARNING",
  "category": "RELIABILITY",
  "message": "A callback was triggered too frequently in a short window",
  "fingerprint": "CALLBACK_STORM:RELIABILITY:WARNING:ab12cd34",
  "occurrenceCount": 1,
  "context": {
    "callbackName": "cartObserver",
    "windowMs": 5000
  },
  "release": {
    "versionName": "1.1.0",
    "versionCode": 42,
    "buildType": "release",
    "flavor": "play",
    "releaseChannel": "production",
    "buildId": "2026.03.21.1"
  },
  "device": {
    "brand": "google",
    "manufacturer": "Google",
    "model": "Pixel 8",
    "androidVersion": "14",
    "sdkInt": 34
  }
}
```

## JSON example: insight event

```json
{
  "eventId": "evt_02",
  "timestamp": 1710000002000,
  "type": "insight",
  "schemaVersion": "2.0",
  "sessionId": "session_1",
  "installId": "install_1",
  "appVersion": "1.1.0",
  "sdkVersion": "2.0.0",
  "insightType": "performance_issue",
  "title": "Slow checkout flow",
  "summary": "Multiple slow traces and main-thread anomalies were observed during checkout",
  "probableCause": "Heavy work is happening on the main thread",
  "impact": "The checkout flow may feel laggy or unstable",
  "confidence": 0.92,
  "relatedAnomalyCodes": [
    "SLOW_TRACE",
    "MAIN_THREAD_SLOW"
  ],
  "release": {
    "versionName": "1.1.0",
    "versionCode": 42,
    "buildType": "release",
    "releaseChannel": "production"
  },
  "device": {
    "brand": "google",
    "manufacturer": "Google",
    "model": "Pixel 8",
    "androidVersion": "14",
    "sdkInt": 34
  }
}
```

## JSON example: trace event

```json
{
  "eventId": "evt_03",
  "timestamp": 1710000003000,
  "type": "trace",
  "schemaVersion": "2.0",
  "sessionId": "session_1",
  "installId": "install_1",
  "appVersion": "1.1.0",
  "sdkVersion": "2.0.0",
  "name": "checkout",
  "durationMs": 2180,
  "status": "ok",
  "tags": {
    "step": "payment"
  },
  "release": {
    "versionName": "1.1.0",
    "versionCode": 42,
    "buildType": "release",
    "releaseChannel": "production"
  },
  "device": {
    "brand": "google",
    "manufacturer": "Google",
    "model": "Pixel 8",
    "androidVersion": "14",
    "sdkInt": 34
  }
}
```

## JSON example: track event

```json
{
  "eventId": "evt_04",
  "timestamp": 1710000004000,
  "type": "track",
  "schemaVersion": "2.0",
  "sessionId": "session_1",
  "installId": "install_1",
  "appVersion": "1.1.0",
  "sdkVersion": "2.0.0",
  "name": "checkout_started",
  "attributes": {
    "entryPoint": "cart",
    "currency": "USD"
  },
  "release": {
    "versionName": "1.1.0",
    "versionCode": 42,
    "buildType": "release",
    "releaseChannel": "production"
  },
  "device": {
    "brand": "google",
    "manufacturer": "Google",
    "model": "Pixel 8",
    "androidVersion": "14",
    "sdkInt": 34
  }
}
```

## Build metadata injection

Some release fields can be detected centrally:

- `appVersion` from Android package info
- `versionCode` from Android package info

Some fields are app-specific and should be injected through config:

- `buildType`
- `flavor`
- `releaseChannel`
- `buildId`

Recommended Java setup:

```java
FoxTelemetryConfig config = new FoxTelemetryConfig.Builder()
        .projectId("demo-project")
        .appId("android-main")
        .packageName(getPackageName())
        .endpoint("https://api.example.com/api/v1/ingest")
        .ingestKey("ft_ingest_key")
        .buildType(BuildConfig.BUILD_TYPE)
        .flavor(BuildConfig.FLAVOR)
        .releaseChannel("production")
        .buildId(String.valueOf(BuildConfig.VERSION_CODE))
        .build();
```

Recommended JSON asset setup:

```json
{
  "foxTelemetry": {
    "projectId": "demo-project",
    "appId": "android-main",
    "packageName": "com.example.app",
    "endpoint": "https://api.example.com/api/v1/ingest",
    "ingestKey": "ft_ingest_key",
    "buildType": "release",
    "flavor": "play",
    "releaseChannel": "production",
    "buildId": "2026.03.21.1"
  }
}
```
