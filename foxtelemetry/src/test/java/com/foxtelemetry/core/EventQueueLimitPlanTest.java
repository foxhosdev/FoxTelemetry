package com.foxtelemetry.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EventQueueLimitPlanTest {

    @Test
    public void purgesOldestWhenCountExceeded() {
        List<Long> lengths = new ArrayList<>();
        for (int i = 0; i < 10_020; i++) lengths.add(20L); // small payloads

        int purge = EventQueue.planPurgeCount(lengths);
        assertEquals(20, purge);

        // simulate removal and ensure limits satisfied
        List<Long> remaining = lengths.subList(purge, lengths.size());
        long count = remaining.size();
        long bytes = remaining.stream().mapToLong(Long::longValue).sum();
        assertTrue(count <= 10_000);
        assertTrue(bytes <= 10L * 1024 * 1024);
    }

    @Test
    public void purgesEnoughWhenBytesExceeded() {
        List<Long> lengths = new ArrayList<>();
        // ~50KB per event, enough to push over 10MB with 250 events
        for (int i = 0; i < 250; i++) lengths.add(50_000L);

        int purge = EventQueue.planPurgeCount(lengths);
        assertTrue(purge > 0);

        List<Long> remaining = lengths.subList(purge, lengths.size());
        long count = remaining.size();
        long bytes = remaining.stream().mapToLong(Long::longValue).sum();

        assertTrue(count <= 10_000);
        assertTrue(bytes <= 10L * 1024 * 1024);
    }
}
