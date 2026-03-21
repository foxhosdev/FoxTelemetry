package com.foxtelemetry.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.foxtelemetry.api.FoxTelemetryConfig;

import org.junit.Test;

public class FoxTelemetryConfigCompatTest {

    @Test
    public void legacyConfigMapsToPublicConfig() {
        com.foxtelemetry.core.FoxTelemetryConfig legacy = new com.foxtelemetry.core.FoxTelemetryConfig(
                "project",
                "app",
                "pkg",
                "https://example.com/ingest",
                "key",
                "prod",
                "user-1",
                true,
                80,
                false
        );

        FoxTelemetryConfig mapped = legacy.toPublicConfig();

        assertEquals("project", mapped.projectId);
        assertEquals("app", mapped.appId);
        assertEquals("pkg", mapped.packageName);
        assertEquals("https://example.com/ingest", mapped.endpoint);
        assertEquals("key", mapped.ingestKey);
        assertEquals("prod", mapped.environment);
        assertEquals("user-1", mapped.userId);
        assertTrue(mapped.enableCrashCapture);
        assertEquals(80, mapped.maxStackFrames);
        assertFalse(mapped.allowHttp);
    }
}
