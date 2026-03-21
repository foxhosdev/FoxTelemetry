package com.foxtelemetry.intelligence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.foxtelemetry.model.LogEvent;
import com.foxtelemetry.model.TrackEvent;

import org.junit.Test;

import java.util.Collections;

public class SignalNoiseControllerTest {

    @Test
    public void limitsDuplicateLogsInsideWindow() {
        SignalNoiseController controller = new SignalNoiseController(30_000L, 2, 3, 5, 5);

        assertTrue(controller.shouldStore(new LogEvent("INFO", "APP", "same")));
        assertTrue(controller.shouldStore(new LogEvent("INFO", "APP", "same")));
        assertFalse(controller.shouldStore(new LogEvent("INFO", "APP", "same")));
        assertFalse(controller.shouldStore(new LogEvent("INFO", "APP", "same")));
        assertTrue(controller.shouldStore(new LogEvent("INFO", "APP", "same")));
    }

    @Test
    public void keepsHigherVolumeTrackSignalsSampled() {
        SignalNoiseController controller = new SignalNoiseController(30_000L, 2, 3, 2, 2);

        assertTrue(controller.shouldStore(new TrackEvent("checkout_started", Collections.<String, String>emptyMap())));
        assertTrue(controller.shouldStore(new TrackEvent("checkout_started", Collections.<String, String>emptyMap())));
        assertFalse(controller.shouldStore(new TrackEvent("checkout_started", Collections.<String, String>emptyMap())));
        assertTrue(controller.shouldStore(new TrackEvent("checkout_started", Collections.<String, String>emptyMap())));
    }
}
