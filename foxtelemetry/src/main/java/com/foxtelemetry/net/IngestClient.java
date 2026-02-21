package com.foxtelemetry.net;

import com.foxtelemetry.core.FoxTelemetryConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class IngestClient {

    private IngestClient() {}

    public static int sendBatch(FoxTelemetryConfig cfg, List<JSONObject> events) throws Exception {
        if (isHttpEndpoint(cfg.endpoint) && !cfg.allowHttp) {
            android.util.Log.w("FoxTelemetryNet", "Endpoint uses HTTP and allowHttp=false; skipping send.");
            return 0; // treated as dropped / disabled
        }

        JSONObject payload = new JSONObject();
        payload.put("projectId", cfg.projectId);
        payload.put("appId", cfg.appId);
        payload.put("packageName", cfg.packageName);

        JSONArray arr = new JSONArray();
        for (JSONObject e : events) arr.put(e);
        payload.put("events", arr);

        byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
        boolean useGzip = true;
        byte[] encodedBody = useGzip ? gzip(body) : body;

        URL url = new URL(cfg.endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("User-Agent", "FoxTelemetry-Android/1.0.3");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("X-Fox-Ingest-Key", cfg.ingestKey);
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

    private static boolean isHttpEndpoint(String endpoint) {
        return endpoint != null && endpoint.trim().toLowerCase().startsWith("http://");
    }

    private static byte[] gzip(byte[] data) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(baos)) {
            gzip.write(data);
        }
        return baos.toByteArray();
    }
}
