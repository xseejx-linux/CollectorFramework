package io.github.xseejx.collectorframework.internal;

/**
 * Class: CollectorNotFoundException
 * For handlign exception; The collector name wasn't found
 */
public class CollectorNotFoundException extends RuntimeException {
    public CollectorNotFoundException(String collectorName) {
        super("Collector not found: " + collectorName);
    }
}
