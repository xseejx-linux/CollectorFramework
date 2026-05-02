package io.github.xseejx.colletctorframework.core.request;

import java.util.Map;

public class CollectorRequest {

    private final String collectorName;
    private final Map<String, Object> parameters;

    public CollectorRequest(String collectorName, Map<String, Object> parameters) {
        this.collectorName = collectorName;
        this.parameters = parameters;
    }


    public Map<String, Object> getParameters() {
        return parameters;
    }
    // get the name of the collector name + parameters

    public String getCollectorName() {
        return collectorName;
    }
}
