package com.foxtelemetry.net;

import com.foxtelemetry.api.FoxTelemetryConfig;
import com.foxtelemetry.core.FoxTelemetryContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class IngestClient {
    static final int RESULT_BLOCKED_BY_POLICY = -1;

    private IngestClient() {}

    public static int sendBatch(FoxTelemetryConfig cfg, List<JSONObject> events) throws Exception {
        if (isHttpEndpoint(cfg.endpoint) && !cfg.allowHttp) {
            return RESULT_BLOCKED_BY_POLICY;
        }

        JSONObject payload = buildBatchPayload(cfg, events);
        byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
        boolean useGzip = true;
        byte[] encodedBody = useGzip ? gzip(body) : body;

        URL url = new URL(cfg.endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("User-Agent", FoxTelemetryContract.SDK_NAME + "/" + FoxTelemetryContract.SDK_VERSION);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("X-Fox-Ingest-Key", cfg.ingestKey);
        conn.setRequestProperty("X-Fox-SDK-Name", FoxTelemetryContract.SDK_NAME);
        conn.setRequestProperty("X-Fox-SDK-Version", FoxTelemetryContract.SDK_VERSION);
        conn.setRequestProperty("X-Fox-Schema-Version", resolveSchemaVersion(events));
        conn.setRequestProperty("X-Fox-Platform", FoxTelemetryContract.PLATFORM);
        conn.setRequestProperty("X-Fox-Client-Package-Version", resolveClientPackageVersion(events));
        setOptionalHeader(conn, "X-Fox-Build-Type", resolveReleaseField(events, "buildType"));
        setOptionalHeader(conn, "X-Fox-Release-Channel", resolveReleaseField(events, "releaseChannel"));
        conn.setRequestProperty("Accept", "application/json");
        if (useGzip) conn.setRequestProperty("Content-Encoding", "gzip");

        try (BufferedOutputStream os = new BufferedOutputStream(conn.getOutputStream())) {
            os.write(encodedBody);
            os.flush();
        }

        int code = conn.getResponseCode();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8
            ));
            while (br.readLine() != null) {}
            br.close();
        } catch (Exception ignored) {}

        conn.disconnect();
        return code;
    }

    static JSONObject buildBatchPayload(FoxTelemetryConfig cfg, List<JSONObject> events) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("projectId", cfg.projectId);
        payload.put("appId", cfg.appId);
        payload.put("packageName", cfg.packageName);
        payload.put("batchId", buildBatchId(cfg, events));
        payload.put("sentAt", System.currentTimeMillis());
        payload.put("apiVersion", FoxTelemetryContract.INGEST_API_VERSION);
        payload.put("eventCount", events.size());

        JSONArray arr = new JSONArray();
        for (JSONObject e : events) arr.put(e);
        payload.put("events", arr);
        return payload;
    }

    static String buildBatchId(FoxTelemetryConfig cfg, List<JSONObject> events) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        updateDigest(digest, cfg.projectId);
        updateDigest(digest, cfg.appId);
        updateDigest(digest, cfg.packageName);

        for (JSONObject event : events) {
            updateDigest(digest, event.optString("eventId", ""));
            updateDigest(digest, Long.toString(event.optLong("timestamp", 0L)));
        }

        byte[] hashed = digest.digest();
        StringBuilder sb = new StringBuilder(hashed.length * 2);
        for (byte b : hashed) {
            sb.append(String.format(Locale.US, "%02x", b & 0xff));
        }

        if (sb.length() == 0) {
            return UUID.randomUUID().toString();
        }
        return sb.toString();
    }

    private static boolean isHttpEndpoint(String endpoint) {
        return endpoint != null && endpoint.trim().toLowerCase().startsWith("http://");
    }

    private static void setOptionalHeader(HttpURLConnection conn, String name, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        conn.setRequestProperty(name, value);
    }

    private static String resolveSchemaVersion(List<JSONObject> events) {
        JSONObject first = firstEvent(events);
        if (first != null) {
            String value = first.optString("schemaVersion", null);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return FoxTelemetryContract.SCHEMA_VERSION;
    }

    private static String resolveClientPackageVersion(List<JSONObject> events) {
        JSONObject first = firstEvent(events);
        if (first != null) {
            String value = first.optString("appVersion", null);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "unknown";
    }

    private static String resolveReleaseField(List<JSONObject> events, String fieldName) {
        JSONObject first = firstEvent(events);
        if (first == null) {
            return null;
        }

        String directValue = first.optString(fieldName, null);
        if (directValue != null && !directValue.trim().isEmpty()) {
            return directValue;
        }

        JSONObject release = first.optJSONObject("release");
        if (release == null) {
            return null;
        }

        String nestedValue = release.optString(fieldName, null);
        return nestedValue != null && !nestedValue.trim().isEmpty() ? nestedValue : null;
    }

    private static JSONObject firstEvent(List<JSONObject> events) {
        return events == null || events.isEmpty() ? null : events.get(0);
    }

    private static void updateDigest(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) '\n');
    }

    private static byte[] gzip(byte[] data) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(baos)) {
            gzip.write(data);
        }
        return baos.toByteArray();
    }
}
