package com.foxtelemetry.api;

import org.junit.Test;

public class FoxTelemetryApiTest {

    @Test(expected = UnsupportedOperationException.class)
    public void measureMemoryIsExplicitlyUnsupported() {
        FoxTelemetry.measureMemory("checkout", new Runnable() {
            @Override
            public void run() {
                // no-op
            }
        });
    }
}
