package com.foxtelemetry.net;

import com.foxtelemetry.core.FoxTelemetryConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 30)
public class IngestClientTest {

    @Test
    public void rejectsHttpWhenNotAllowed() throws Exception {
        FoxTelemetryConfig cfg = new FoxTelemetryConfig(
                "p", "a", "pkg",
                "http://example.com/ingest",
                "key", null, null, true, 80, false);

        int code = IngestClient.sendBatch(cfg, Collections.emptyList());
        assertEquals(0, code);
    }

    @Test
    public void gzipRoundTrip() throws Exception {
        byte[] input = "{\"a\":1}".getBytes("UTF-8");
        byte[] gz = callGzipForTest(input);
        byte[] out = new byte[input.length];
        try (GZIPInputStream gis = new GZIPInputStream(new java.io.ByteArrayInputStream(gz))) {
            int n = gis.read(out);
            assertArrayEquals(input, out);
            assertTrue(n > 0);
        }
    }

    // Access gzip via reflection (private helper)
    private byte[] callGzipForTest(byte[] data) throws Exception {
        java.lang.reflect.Method m = IngestClient.class.getDeclaredMethod("gzip", byte[].class);
        m.setAccessible(true);
        return (byte[]) m.invoke(null, data);
    }
}
