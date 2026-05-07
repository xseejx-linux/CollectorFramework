package io.github.xseejx.collectorframework;

// IMPORTS
import java.util.Map;
//

/**
 * Class: ServiceModel
 * A class defining the ServiceModel, to implement inside the application, to be able to make requests
 * (ONLY FOR SERVICEMANAGER)
 */
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
