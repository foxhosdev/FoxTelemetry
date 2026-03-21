package com.foxtelemetry.intelligence.scoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.AnomalyCategory;
import com.foxtelemetry.model.AnomalyEvent;
import com.foxtelemetry.model.Severity;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class QualityScoreCalculatorTest {

    @Test
    public void calculatesRepeatedExceptionPenaltyOncePerSession() {
        QualityScoreCalculator calculator = new QualityScoreCalculator();
        AnomalyEvent first = new AnomalyEvent(new Anomaly(
                "REPEATED_EXCEPTION",
                "Repeated exception detected",
                Severity.WARNING,
                AnomalyCategory.RELIABILITY,
                "Repeated exception"
        ));
        AnomalyEvent duplicate = new AnomalyEvent(new Anomaly(
                "REPEATED_EXCEPTION",
                "Repeated exception detected",
                Severity.ERROR,
                AnomalyCategory.RELIABILITY,
                "Repeated exception"
        ));

        QualityScore score = calculator.calculate("session-1", Arrays.asList(first, duplicate));

        assertEquals(90, score.reliabilityScore);
        assertEquals(100, score.performanceScore);
        assertEquals(95, score.overallScore);
        assertEquals("session-1", score.sessionId);
        assertEquals(Collections.singletonList("REPEATED_EXCEPTION"), score.appliedPenaltyCodes);
    }

    @Test
    public void calculatesPerformancePenaltiesForCallbackStormAndSlowTrace() {
        QualityScoreCalculator calculator = new QualityScoreCalculator();
        AnomalyEvent callbackStorm = new AnomalyEvent(new Anomaly(
                "CALLBACK_STORM",
                "Callback storm detected",
                Severity.WARNING,
                AnomalyCategory.LIFECYCLE,
                "Too many callbacks"
        ));
        AnomalyEvent slowTrace = new AnomalyEvent(new Anomaly(
                "SLOW_TRACE",
                "Slow trace detected",
                Severity.WARNING,
                AnomalyCategory.PERFORMANCE,
                "Trace took too long"
        ));

        QualityScore score = calculator.calculate("session-2", Arrays.asList(callbackStorm, slowTrace));

        assertEquals(100, score.reliabilityScore);
        assertEquals(77, score.performanceScore);
        assertEquals(88, score.overallScore);
        assertTrue(score.appliedPenaltyCodes.contains("CALLBACK_STORM"));
        assertTrue(score.appliedPenaltyCodes.contains("SLOW_TRACE"));
    }

    @Test
    public void keepsPerfectScoreWhenThereAreNoAnomalies() {
        QualityScoreCalculator calculator = new QualityScoreCalculator();

        QualityScore score = calculator.calculate("session-3", Collections.<AnomalyEvent>emptyList());

        assertEquals(100, score.reliabilityScore);
        assertEquals(100, score.performanceScore);
        assertEquals(100, score.overallScore);
        assertTrue(score.appliedPenaltyCodes.isEmpty());
    }
}
