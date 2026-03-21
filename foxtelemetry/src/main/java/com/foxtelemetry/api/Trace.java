package com.foxtelemetry.api;

import androidx.annotation.NonNull;
import java.util.Map;

/**
 * Public interface for performance tracing.
 */
public interface Trace {
    void end();
    void end(@NonNull String status);
    void putTag(@NonNull String key, @NonNull String value);
}
