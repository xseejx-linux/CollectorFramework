package io.github.xseejx.colletctorframework.core.engine;

public class CollectorNotFoundException extends RuntimeException {
    public CollectorNotFoundException(String collectorName) {
        super("Collector not found: " + collectorName);
    }

}
