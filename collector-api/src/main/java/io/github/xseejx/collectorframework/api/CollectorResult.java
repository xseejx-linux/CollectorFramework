package io.github.xseejx.collectorframework.api;


// IMPORTS
import java.time.Instant;
import org.json.simple.JSONObject;
// --



/**
 * Class: CollectorResult
 * Class which will be used to create an actual result
 * Methods: failure(), ok()
 */
public class CollectorResult {
    private final String collectorName;
    private final JSONObject json;

    private final boolean success;
    private final JSONObject errorMessage;

    private final Instant timestamp;

    /**
     * Constructor for CollectorResult.
     * @param collectorName
     * @param json
     * @param success
     * @param errorMessage
     * @param timestamp
     */
    private CollectorResult(String collectorName, JSONObject json, boolean success, JSONObject errorMessage, Instant timestamp) {
        this.collectorName = collectorName;
        this.json = json;
        this.success = success;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
    }

    /**
     * Factory method for success result.
     * @param name
     * @param result
     * @return
     */
    public static CollectorResult ok(String name, JSONObject result) {
        String collectorName = name;
        JSONObject json = result;
        Boolean success = true;
        JSONObject errorMessage = null;
        Instant timestamp = Instant.now();
        return new CollectorResult(collectorName, json, success, errorMessage, timestamp);
    }



    /**
     * Factory method for failure result.
     * @param name
     * @param result
     * @return
     */
    public static CollectorResult failure(String name, JSONObject result) {
        String collectorName = name;
        JSONObject json = null;
        Boolean success = false;
        JSONObject errorMessage = result;
        Instant timestamp = Instant.now();
        return new CollectorResult(collectorName, json, success, errorMessage, timestamp);
    }


    /**
     * Convert this CollectorResult to a JSON object for output.
     * @return
     */
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
