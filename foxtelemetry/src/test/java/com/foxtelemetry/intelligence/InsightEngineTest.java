package com.foxtelemetry.intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.AnomalyCategory;
import com.foxtelemetry.model.AnomalyEvent;
import com.foxtelemetry.model.Severity;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InsightEngineTest {

    @Test
    public void mapsSlowTraceToPerformanceIssue() {
        InsightEngine engine = new InsightEngine(1_000L);
        AnomalyEvent slowTrace = new AnomalyEvent(new Anomaly(
                "SLOW_TRACE",
                "Slow operation detected",
                Severity.WARNING,
                AnomalyCategory.PERFORMANCE,
                "Too slow"
        ));

        List<Insight> insights = engine.evaluate("session-1", Collections.singletonList(slowTrace), System.currentTimeMillis());

        assertEquals(1, insights.size());
        assertEquals("performance_issue", insights.get(0).type);
        assertEquals("Slow operation detected", insights.get(0).title);
    }

    @Test
    public void mapsCallbackStormAndSlowTraceToRuntimeLoopIssue() {
        InsightEngine engine = new InsightEngine(1_000L);
        AnomalyEvent callbackStorm = new AnomalyEvent(new Anomaly(
                "CALLBACK_STORM",
                "Callback storm detected",
                Severity.WARNING,
                AnomalyCategory.LIFECYCLE,
                "Too many callbacks"
        ));
        AnomalyEvent slowTrace = new AnomalyEvent(new Anomaly(
                "SLOW_TRACE",
                "Slow operation detected",
                Severity.WARNING,
                AnomalyCategory.PERFORMANCE,
                "Too slow"
        ));

        List<Insight> insights = engine.evaluate("session-1", Arrays.asList(callbackStorm, slowTrace), System.currentTimeMillis());

        assertEquals(2, insights.size());
        assertTrue(containsType(insights, "performance_issue"));
        assertTrue(containsType(insights, "runtime_loop_issue"));
    }

    @Test
    public void mapsRepeatedExceptionToUnstableFlow() {
        InsightEngine engine = new InsightEngine(1_000L);
        AnomalyEvent repeatedException = new AnomalyEvent(new Anomaly(
                "REPEATED_EXCEPTION",
                "Repeated exception detected",
                Severity.WARNING,
                AnomalyCategory.RELIABILITY,
                "Exception repeated"
        ));

        List<Insight> insights = engine.evaluate("session-1", Collections.singletonList(repeatedException), System.currentTimeMillis());

        assertEquals(1, insights.size());
        assertEquals("unstable_flow", insights.get(0).type);
    }

    private boolean containsType(List<Insight> insights, String type) {
        for (Insight insight : insights) {
            if (type.equals(insight.type)) {
                return true;
            }
        }
        return false;
    }
}
