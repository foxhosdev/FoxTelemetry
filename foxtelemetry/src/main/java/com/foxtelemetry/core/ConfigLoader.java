package com.foxtelemetry.core;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class ConfigLoader {

    private ConfigLoader() {}

    @Nullable
    public static FoxTelemetryConfig loadFromAssets(@NonNull Context context, @NonNull String assetFileName) {
        try {
            AssetManager am = context.getAssets();
            try (InputStream is = am.open(assetFileName);
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject root = new JSONObject(sb.toString());
                JSONObject fox = root.getJSONObject("foxTelemetry");

                String projectId = fox.getString("projectId");
                String appId = fox.getString("appId");
                String packageName = fox.getString("packageName");
                String endpoint = fox.getString("endpoint");
                String ingestKey = fox.getString("ingestKey");
                String environment = fox.optString("environment", null);
                boolean allowHttp = fox.optBoolean("allowHttp", false);

                return new FoxTelemetryConfig(
                        projectId,
                        appId,
                        packageName,
                        endpoint,
                        ingestKey,
                        environment,
                        null,
                        true,
                        80,
                        allowHttp
                );
            }
        } catch (Exception e) {
            return null;
        }
    }
}
