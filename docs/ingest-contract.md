# FoxTelemetry Ingest Contract

## Purpose

This document defines the HTTP contract expected by the future backend for FoxTelemetry Android SDK batches.

It is intentionally backend-facing and should be sufficient to implement ingestion without reading Android source files.

## Endpoint expectation

HTTP method:

- `POST`

Endpoint shape:

- `/api/v1/ingest`

The path version belongs to the backend ingest API contract, not to the SDK runtime version.

## Request headers

Required headers sent by the Android SDK:

- `Content-Type: application/json; charset=utf-8`
- `Accept: application/json`
- `X-Fox-Ingest-Key: <project write token>`
- `X-Fox-SDK-Name: FoxTelemetry-Android`
- `X-Fox-SDK-Version: 2.0.0`
- `X-Fox-Schema-Version: 2.0`
- `X-Fox-Platform: android`
- `X-Fox-Client-Package-Version: <app version name when available>`

Optional headers when available:

- `X-Fox-Build-Type`
- `X-Fox-Release-Channel`

Transport headers:

- `User-Agent: FoxTelemetry-Android/2.0.0`
- `Content-Encoding: gzip` when batch compression is enabled

## Authentication expectation

- the backend authenticates the request with `X-Fox-Ingest-Key`
- the ingest key should be treated as a project write token
- the backend should store only a hash of the ingest key
- the backend should support key rotation and revocation

## Batch structure

The root request body is JSON with this structure:

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

Required root fields:

- `projectId`
- `appId`
- `packageName`
- `batchId`
- `sentAt`
- `apiVersion`
- `eventCount`
- `events`

## Batch metadata semantics

- `batchId`: deterministic identifier derived from project/app/package and event identities
- `sentAt`: client-side send time in epoch milliseconds
- `apiVersion`: current ingest API contract version, currently `v1`
- `eventCount`: count of events in the array

## Event validation expectations

The backend should validate at least:

- request auth key exists and is active
- `projectId` matches the key scope
- `appId` matches the key scope
- `packageName` matches the registered app
- `events` is an array
- `eventCount == events.length`
- every event has:
- `eventId`
- `timestamp`
- `type`
- `schemaVersion`

The backend should also:

- accept backward-compatible optional fields
- tolerate new optional event fields under the same `schemaVersion`
- reject or dead-letter unsupported `schemaVersion` values
- keep the raw event payload for forensic/debug use
- extract indexed fields for queries and aggregations

## Event types currently expected

- `error`
- `log`
- `track`
- `trace`
- `anomaly`
- `insight`

## Device and release metadata expectation

The backend should expect event-level metadata such as:

- `appVersion`
- `versionCode`
- `buildType`
- `flavor`
- `releaseChannel`
- `buildId`
- `device.brand`
- `device.manufacturer`
- `device.model`
- `device.androidVersion`
- `device.sdkInt`

## Expected backend responses

Recommended success response:

- `202 Accepted` when the batch is accepted for processing/storage

Recommended auth failures:

- `401 Unauthorized` when the ingest key is missing or malformed
- `403 Forbidden` when the ingest key is known but revoked or not allowed for the target scope

Recommended validation failures:

- `422 Unprocessable Entity` when the body or event schema is invalid

Recommended throttling:

- `429 Too Many Requests` when rate limits or quotas are exceeded

Recommended server failure:

- `5xx` only for real transient backend failures

## Failure and retry expectations

The current Android client behavior is:

- retries on `429`
- retries on `5xx`
- does not retry on other `4xx`
- drops batches blocked by client policy

Backend implication:

- avoid accidental `4xx` responses for transient server-side problems
- use `429` for quota/rate limiting
- use `422` only when the payload is truly invalid
- use `401` or `403` only for real auth or authorization problems

## Compatibility expectations

Backend parsing rules:

- primary compatibility key: `schemaVersion`
- support at least the current stable schema and one previous schema when possible
- log unknown combinations of `sdkVersion` and `schemaVersion`
- do not parse based only on `sdkVersion`

## Implementation notes for the backend

Recommended ingestion flow:

1. authenticate key
2. decompress gzip body when needed
3. validate root batch
4. validate event envelopes
5. store request audit entry
6. store raw events
7. fan out async aggregation and grouping jobs

Recommended storage split:

- raw event store
- indexed query table
- request audit log
- dead-letter store for invalid or unsupported payloads
