package com.foxtelemetry.collectors.anomaly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.foxtelemetry.model.Anomaly;
import com.foxtelemetry.model.AnomalyCategory;
import com.foxtelemetry.model.Severity;

import org.junit.Test;

public class AnomalyManagerTest {

    @Test
    public void deduplicatesByFingerprintAndSession() {
        AnomalyManager manager = new AnomalyManager();

        Anomaly first = new Anomaly(
                "SLOW_TRACE",
                "Slow trace",
                Severity.WARNING,
                AnomalyCategory.PERFORMANCE,
                "Operation was slow"
        );
        assertTrue(manager.shouldReport(first, "session-1", "install-1"));
        assertEquals(1, first.occurrenceCount);

        Anomaly duplicateSameSession = new Anomaly(
                "SLOW_TRACE",
                "Slow trace",
                Severity.WARNING,
                AnomalyCategory.PERFORMANCE,
                "Operation was slow"
        );
        assertFalse(manager.shouldReport(duplicateSameSession, "session-1", "install-1"));
        assertEquals(2, duplicateSameSession.occurrenceCount);

        Anomaly sameFingerprintDifferentSession = new Anomaly(
                "SLOW_TRACE",
                "Slow trace",
                Severity.WARNING,
                AnomalyCategory.PERFORMANCE,
                "Operation was slow"
        );
        assertTrue(manager.shouldReport(sameFingerprintDifferentSession, "session-2", "install-1"));
        assertEquals(1, sameFingerprintDifferentSession.occurrenceCount);
    }
}
