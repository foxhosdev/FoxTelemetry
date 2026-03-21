package com.foxtelemetry.intelligence.rules;

import androidx.annotation.NonNull;

import com.foxtelemetry.intelligence.EventCorrelationWindow;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.BaseEvent;

import java.util.List;

public interface Rule {
    @NonNull
    String getId();

    @NonNull
    List<Anomaly> evaluate(@NonNull BaseEvent event, @NonNull EventCorrelationWindow window);
}
