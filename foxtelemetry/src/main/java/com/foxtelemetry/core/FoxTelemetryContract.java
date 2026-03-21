package com.foxtelemetry.core;

import androidx.annotation.NonNull;

/**
 * Shared transport constants for the FoxTelemetry SDK.
 */
public final class FoxTelemetryContract {
    @NonNull public static final String SDK_NAME = "FoxTelemetry-Android";
    @NonNull public static final String SDK_VERSION = "2.0.0";
    @NonNull public static final String SCHEMA_VERSION = "2.0";
    @NonNull public static final String INGEST_API_VERSION = "v1";
    @NonNull public static final String PLATFORM = "android";

    private FoxTelemetryContract() {}
}
