package com.foxtelemetry.net;

import com.foxtelemetry.api.FoxTelemetryConfig;

import org.junit.Test;

import java.util.Collections;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IngestClientTest {

    @Test
    public void rejectsHttpWhenNotAllowed() throws Exception {
        FoxTelemetryConfig cfg = new FoxTelemetryConfig.Builder()
                .projectId("p")
                .appId("a")
                .packageName("pkg")
                .endpoint("http://example.com/ingest")
                .ingestKey("key")
                .allowHttp(false)
                .build();

        int code = IngestClient.sendBatch(cfg, Collections.emptyList());
        assertEquals(-1, code);
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
