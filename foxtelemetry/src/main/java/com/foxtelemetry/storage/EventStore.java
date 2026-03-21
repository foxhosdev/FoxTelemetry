package com.foxtelemetry.storage;

import androidx.annotation.NonNull;
import com.foxtelemetry.model.BaseEvent;
import org.json.JSONObject;
import java.util.List;

/**
 * Interface for storing events before they are dispatched.
 */
public interface EventStore {
    
    /**
     * Store an event in the local storage.
     */
    void enqueue(@NonNull BaseEvent event) throws Exception;

    /**
     * Get a batch of events from storage.
     */
    List<JSONObject> peek(int max) throws Exception;

    /**
     * Remove events from storage after they have been successfully dispatched.
     */
    void drop(int n) throws Exception;

    /**
     * Estimate the number of events in storage.
     */
    int sizeEstimate();
}
