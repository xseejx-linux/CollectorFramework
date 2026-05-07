package io.github.xseejx.colletctorframework.core.service;

import java.util.Map;

public class ServiceModel {

    private final String collectorName;
    private final Map<String, Object> parameters;

    public ServiceModel(String collectorName, Map<String, Object> parameters) {
        this.collectorName = collectorName;
        this.parameters = parameters;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getCollectorName() {
        return collectorName;
    }
}
