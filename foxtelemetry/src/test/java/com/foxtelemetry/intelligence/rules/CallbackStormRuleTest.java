package com.foxtelemetry.intelligence.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.foxtelemetry.intelligence.CorrelationEngine;
import com.foxtelemetry.intelligence.EventCorrelationWindow;
import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.TrackEvent;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class CallbackStormRuleTest {

    @Test
    public void emitsCallbackStormAnomalyWhenThresholdReached() {
        CallbackStormRule rule = new CallbackStormRule(4, 5_000L, 1_000L);
        CorrelationEngine correlationEngine = new CorrelationEngine(50, 30_000L);

        List<Anomaly> outputs = java.util.Collections.emptyList();
        for (int i = 0; i < 4; i++) {
            TrackEvent event = new TrackEvent("observer_callback", Collections.<String, String>emptyMap());
            event.sessionId = "session-1";
            EventCorrelationWindow window = correlationEngine.record(event);
            outputs = rule.evaluate(event, window);
        }

        assertEquals(1, outputs.size());
        assertEquals("CALLBACK_STORM", outputs.get(0).code);
        assertTrue(outputs.get(0).context.containsKey("eventName"));
    }
}
