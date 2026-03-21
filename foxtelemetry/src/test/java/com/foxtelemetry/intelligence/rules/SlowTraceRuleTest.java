package com.foxtelemetry.intelligence.rules;

import static org.junit.Assert.assertEquals;

import com.foxtelemetry.intelligence.CorrelationEngine;
import com.foxtelemetry.intelligence.EventCorrelationWindow;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.Severity;
import com.foxtelemetry.model.TraceEvent;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class SlowTraceRuleTest {

    @Test
    public void emitsSlowTraceAnomaly() {
        SlowTraceRule rule = new SlowTraceRule(2_000L, 1_000L);
        CorrelationEngine correlationEngine = new CorrelationEngine(50, 30_000L);

        TraceEvent trace = new TraceEvent("screen_load", 2_500L, "ok", Collections.<String, String>emptyMap());
        trace.sessionId = "session-1";
        EventCorrelationWindow window = correlationEngine.record(trace);

        List<Anomaly> outputs = rule.evaluate(trace, window);

        assertEquals(1, outputs.size());
        assertEquals("SLOW_TRACE", outputs.get(0).code);
        assertEquals(Severity.WARNING, outputs.get(0).severity);
    }
}
