package io.github.xseejx.colletctorframework.core.api;

import java.time.Instant;
import org.json.simple.JSONObject;;

public class CollectorResult {
    private final String collectorName;
    private final JSONObject json;

    private final boolean success;
    private final JSONObject errorMessage;

    private final Instant timestamp;

    CollectorResult(String collectorName, JSONObject json, boolean success, JSONObject errorMessage, Instant timestamp) {
        this.collectorName = collectorName;
        this.json = json;
        this.success = success;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
    }


    public static CollectorResult ok(String name, JSONObject result) {
        String collectorName = name;
        JSONObject json = result;
        Boolean success = true;
        JSONObject errorMessage = null;
        Instant timestamp = Instant.now();
        return new CollectorResult(collectorName, json, success, errorMessage, timestamp);
    }




    public static CollectorResult failure(String name, JSONObject result) {
        String collectorName = name;
        JSONObject json = null;
        Boolean success = false;
        JSONObject errorMessage = result;
        Instant timestamp = Instant.now();
        return new CollectorResult(collectorName, json, success, errorMessage, timestamp);
    }

    @SuppressWarnings("unchecked")
    public JSONObject getResult() {
        JSONObject result = new JSONObject();
        result.put("collectorName", collectorName);
        result.put("success", success);
        result.put("errorMessage", errorMessage);
        result.put("timestamp", timestamp.toString());
        if (success) {
            result.put("data", json);
        }
        return result;
    }
}
