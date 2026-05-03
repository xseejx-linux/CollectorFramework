package io.github.xseejx.colletctorframework.core.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import io.github.xseejx.colletctorframework.core.api.CollectorResult;
import io.github.xseejx.colletctorframework.core.engine.CollectorEngine;
import io.github.xseejx.colletctorframework.core.registry.CollectorRegistry;

public class CollectorRequestActivator {

    private CollectorRegistry registry = new CollectorRegistry();

    public String activateRequest(String collectorName, Map<String, Object> parameters) {
        listAvailable();
        
        CollectorRequest request = new CollectorRequest(collectorName, parameters);
        AtomicReference<String> jsonResponse = new AtomicReference<>("{}");
        
        registry.get(collectorName).ifPresent(collector -> {
            CollectorEngine engine = new CollectorEngine(registry);
            Future<CollectorResult> result = engine.execute(request);
            try {
                CollectorResult res = result.get();
                jsonResponse.set(res.getResult().toJSONString());
            } catch (Exception e) {
                jsonResponse.set("{\"error\": \"" + e.getMessage() + "\"}");
            }
            engine.shutdown();
        });

        return jsonResponse.get(); 
    }

    /*
    public List<String> activateRequests(List<Map<String, Map<String, Object>>> requests) {
        listAvailable();
        List<String> results = new ArrayList<>();
        requests.forEach(request -> {
            String collectorName = request.entrySet().iterator().next().getKey();
            Map<String, Object> parameters = request.get(collectorName);
            // System.out.println("Activating collector: " + collectorName + " with parameters: " + parameters);
            String result = activateRequest(collectorName, parameters);
            results.add(result);
        });
        return results;
    }
    */

    public List<String> activateRequestsV2(List<Map<String, Map<String, Object>>> requests) {
        listAvailable();
        List<CollectorRequest> collectorRequests = new ArrayList<>();
        AtomicReference<String> resultsJson = new AtomicReference<>("[]");

        // Process requests and separate collectors from errors
        requests.forEach(request -> {
            String collectorName = request.entrySet().iterator().next().getKey();
            Map<String, Object> parameters = request.get(collectorName);

            

            registry.get(collectorName).ifPresentOrElse((collector -> {
                collectorRequests.add(new CollectorRequest(collectorName, parameters));
            }), () -> {
                return;
            });
        });

        // Execute valid collectors
        CollectorEngine engine = new CollectorEngine(registry);
        Map<String, Future<CollectorResult>> futures = engine.executeAll(collectorRequests);

        futures.forEach((name, result) -> {
            try {
                CollectorResult res = result.get();
                // Add successful result to results array (thread-safe)
                resultsJson.updateAndGet(old -> {
                    String newEntry = "{\"collector\": \"" + name + "\", \"result\": " + res.getResult().toJSONString() + "}";
                    if (old.equals("[]")) {
                        return "[" + newEntry + "]";
                    } else {
                        return old.substring(0, old.length() - 1) + "," + newEntry + "]";
                    }
                });
            } catch (Exception e) {
                // Add error result to results array (thread-safe)
                resultsJson.updateAndGet(old -> {
                    String newEntry = "{\"collector\": \"" + name + "\", \"error\": \"" + e.getMessage() + "\"}";
                    if (old.equals("[]")) {
                        return "[" + newEntry + "]";
                    } else {
                        return old.substring(0, old.length() - 1) + "," + newEntry + "]";
                    }
                });
            }
        });
        engine.shutdown();

        return List.of(resultsJson.get());
    } 


    public List<String> listAvailable() {
        registry.discoverAll();
        return registry.listAvailable().stream().sorted().toList();
    }

}
