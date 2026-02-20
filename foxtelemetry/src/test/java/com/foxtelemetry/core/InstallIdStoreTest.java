package com.foxtelemetry.core;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InstallIdStoreTest {

    @Test
    public void returnsSameIdAcrossCalls() {
        InMemoryStorage storage = new InMemoryStorage();
        String first = InstallIdStore.getOrCreateInstallId(storage);
        String second = InstallIdStore.getOrCreateInstallId(storage);
        assertEquals(first, second);
    }

    @Test
    public void generatesUuidWhenEmpty() {
        InMemoryStorage storage = new InMemoryStorage();
        String id = InstallIdStore.getOrCreateInstallId(storage);
        assertNotNull(id);
        assertTrue("must look like a UUID", id.split("-").length == 5);
    }

    private static final class InMemoryStorage implements InstallIdStore.Storage {
        private final AtomicReference<String> ref = new AtomicReference<>();

        @Override
        public String get() {
            return ref.get();
        }

        @Override
        public void set(String id) {
            ref.set(id);
        }
    }
}
