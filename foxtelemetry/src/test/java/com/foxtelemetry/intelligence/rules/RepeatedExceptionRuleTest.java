package com.foxtelemetry.intelligence.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.foxtelemetry.intelligence.CorrelationEngine;
import com.foxtelemetry.intelligence.EventCorrelationWindow;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.ErrorEvent;
import com.foxtelemetry.model.Severity;

import org.junit.Test;

import java.util.List;

public class RepeatedExceptionRuleTest {

    @Test
    public void emitsRepeatedExceptionAnomalyWhenThresholdReached() {
        RepeatedExceptionRule rule = new RepeatedExceptionRule(3, 10_000L, 1_000L);
        CorrelationEngine correlationEngine = new CorrelationEngine(50, 30_000L);

        List<Anomaly> outputs = java.util.Collections.emptyList();
        for (int i = 0; i < 3; i++) {
            ErrorEvent event = new ErrorEvent(new IllegalStateException("boom"), "checkout", false);
            event.sessionId = "session-1";
            EventCorrelationWindow window = correlationEngine.record(event);
            outputs = rule.evaluate(event, window);
        }

        assertEquals(1, outputs.size());
        assertEquals("REPEATED_EXCEPTION", outputs.get(0).code);
        assertEquals(Severity.WARNING, outputs.get(0).severity);
        assertTrue(outputs.get(0).context.containsKey("count"));
    }
}
