# Changelog

All notable changes to this project will be documented in this file.

## 1.2.0

- Added backend-ready ingest contract documentation
- Added explicit ingest headers for SDK, schema, platform, and client package version
- Added root batch metadata: `batchId`, `sentAt`, `apiVersion`, `eventCount`
- Added release/build metadata enrichment: `versionCode`, `buildType`, `flavor`, `releaseChannel`, `buildId`
- Added device/OS metadata enrichment: `brand`, `manufacturer`, `model`, `androidVersion`, `sdkInt`
- Marked `measureMemory()` as explicitly unsupported until real memory probes are implemented
