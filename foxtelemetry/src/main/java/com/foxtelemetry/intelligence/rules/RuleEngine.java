package com.foxtelemetry.intelligence.rules;

import androidx.annotation.NonNull;

import com.foxtelemetry.intelligence.EventCorrelationWindow;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.BaseEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RuleEngine {
    private final List<Rule> rules;

    public RuleEngine(@NonNull List<Rule> rules) {
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
    }

    @NonNull
    public List<Anomaly> evaluate(@NonNull BaseEvent event, @NonNull EventCorrelationWindow window) {
        List<Anomaly> outputs = new ArrayList<>();
        for (Rule rule : rules) {
            outputs.addAll(rule.evaluate(event, window));
        }
        return outputs;
    }
}
