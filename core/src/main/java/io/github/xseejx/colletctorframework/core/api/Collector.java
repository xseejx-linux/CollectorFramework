package io.github.xseejx.colletctorframework.core.api;

import java.util.Collections;
import java.util.Map;

public interface Collector {
    /**
     * Human-readable name of this collector.
     * Used by core to route requests from shell.
     */
    String getName();

    /**
     * Run the collection and return structured JSON result.
     */
    CollectorResult collect();

    /**
     * Describes what fields/filters this collector accepts.
     * Core uses this for reflective field injection before calling collect().
     */
    default Map<String, Class<?>> getAcceptedParameters() {
        return Collections.emptyMap();
    }

    /**
     * Whether this collector is currently available
     * (e.g. battery collector absent on desktops).
     */
    default boolean isAvailable() {
        return true;
    }
    
}
