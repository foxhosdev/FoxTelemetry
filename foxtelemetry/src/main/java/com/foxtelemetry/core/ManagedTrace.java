package com.foxtelemetry.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.api.Trace;
import com.foxtelemetry.model.TraceEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

final class ManagedTrace implements Trace {
    private final FoxCore core;
    private final String name;
    private final long startedAtMs;
    private final Map<String, String> tags = new LinkedHashMap<>();
    private final AtomicBoolean ended = new AtomicBoolean(false);

    ManagedTrace(@NonNull FoxCore core, @NonNull String name) {
        this.core = core;
        this.name = name;
        this.startedAtMs = System.currentTimeMillis();
        this.core.onTraceStarted(name);
    }

    @Override
    public void end() {
        end("ok");
    }

    @Override
    public void end(@NonNull String status) {
        if (!ended.compareAndSet(false, true)) {
            return;
        }
        try {
            long durationMs = System.currentTimeMillis() - startedAtMs;
            core.dispatch(new TraceEvent(name, durationMs, status, tags));
        } finally {
            core.onTraceEnded();
        }
    }

    @Override
    public void putTag(@NonNull String key, @NonNull String value) {
        if (ended.get()) {
            return;
        }
        tags.put(key, value);
    }
}
