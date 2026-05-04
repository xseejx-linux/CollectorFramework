package io.github.xseejx.colletctorframework.core.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import io.github.xseejx.colletctorframework.core.api.CollectorResult;
import io.github.xseejx.colletctorframework.core.engine.CollectorEngine;
import io.github.xseejx.colletctorframework.core.registry.CollectorRegistry;

//TODO: Later improvement: one long-lived engine, one shared thread pool,per-request execution through that engine
public class CollectorRequestActivator {

    private final CollectorRegistry registry = new CollectorRegistry();

    public String activateRequestSync(String collectorName, Map<String, Object> parameters) {
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


    public CompletableFuture<String> activateRequestAsync(String collectorName, Map<String, Object> parameters) {
        listAvailable();

        CollectorRequest request = new CollectorRequest(collectorName, parameters);
        CollectorEngine engine = new CollectorEngine(registry);


        return engine.executeAsync(request)
            .thenApply(res -> res.getResult().toJSONString())
            .exceptionally(e -> "{\"error\": \"" + e.getMessage() + "\"}")
            .whenComplete((r, e) -> engine.shutdown());
  
    }

    //TODO: must return a json array of results with collector name and result or error for each request
    public List<CompletableFuture<String>> activateRequestsAsync(List<Map<String, Map<String, Object>>> requests) {

        listAvailable();

        List<CompletableFuture<String>> futures = new ArrayList<>();

        CollectorEngine engine = new CollectorEngine(registry);
        

        for (Map<String, Map<String, Object>> request : requests) {

            String collectorName = request.keySet().iterator().next();
            Map<String, Object> parameters = request.get(collectorName);          

            CompletableFuture<String> future = registry.get(collectorName)
                .map(collector -> {

                    CollectorRequest collectorRequest =
                        new CollectorRequest(collectorName, parameters);

                    return engine.executeAsync(collectorRequest)
                        .thenApply(res -> {
                            return "{\"collector\": \"" + collectorName +
                                "\", \"result\": " +
                                res.getResult().toJSONString() + "}";
                        })
                        .exceptionally(e -> {
                            return "{\"collector\": \"" + collectorName +
                                "\", \"error\": \"" +
                                e.getMessage() + "\"}";
                        });

                })
                .orElseGet(() -> CompletableFuture.completedFuture(
                    "{\"collector\": \"" + collectorName +
                    "\", \"error\": \"not found\"}"
                ));

            futures.add(future);
        }
        return futures;
    }



    
    
    

    public List<String> activateRequestsSync(List<Map<String, Map<String, Object>>> requests) {
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


    public List<String> getMetada(String collectorName) {
        listAvailable();
        List<String> results = new ArrayList<>();
        registry.get(collectorName).ifPresent(collector -> {
            // Let dynamicallt add metadata infos
            results.add("Name: " + collector.getName());
            results.add("Description: " + collector.getClass().getAnnotation(io.github.xseejx.colletctorframework.core.api.CollectorMetadata.class).description());
            results.add("Tags: " + String.join(", ", collector.getClass().getAnnotation(io.github.xseejx.colletctorframework.core.api.CollectorMetadata.class).tags()));
            //results.add("Description: " + collector.getClass().getAnnotation(io.github.xseejx.colletctorframework.core.api.CollectorMetadata.class).new_field());
        });
        return results;
    }



    public List<String> listAvailable() {
        registry.discoverAll();
        return registry.listAvailable().stream().sorted().toList();
    }

}
