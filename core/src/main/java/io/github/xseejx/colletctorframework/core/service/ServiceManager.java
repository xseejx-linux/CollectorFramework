package io.github.xseejx.colletctorframework.core.service;

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
public class ServiceManager {

    private final CollectorRegistry registry = new CollectorRegistry();
    private final CollectorEngine engine = new CollectorEngine(registry);
    public String activateServiceSync(String collectorName, Map<String, Object> parameters) {
        listAvailable();
        
        ServiceModel request = new ServiceModel(collectorName, parameters);
        AtomicReference<String> jsonResponse = new AtomicReference<>("{}");
        
        registry.get(collectorName).ifPresent(collector -> {
            
            Future<CollectorResult> result = engine.executeSync(request);
            try {
                CollectorResult res = result.get();
                jsonResponse.set(res.getResult().toJSONString());
            } catch (Exception e) {
                jsonResponse.set("{\"error\": \"" + e.getMessage() + "\"}");
            }
            //engine.shutdown();
        });

        return jsonResponse.get(); 
    }


    public CompletableFuture<String> activateServiceAsync(String collectorName, Map<String, Object> parameters) {
        listAvailable();

        ServiceModel request = new ServiceModel(collectorName, parameters);
        //CollectorEngine engine = new CollectorEngine(registry);


        return engine.executeAsync(request)
            .thenApply(res -> res.getResult().toJSONString())
            .exceptionally(e -> "{\"error\": \"" + e.getMessage() + "\"}");
            //.whenComplete((r, e) -> engine.shutdown());
  
    }

    //TODO: must return a json array of results with collector name and result or error for each request
    public List<CompletableFuture<String>> activateServicesAsync(List<Map<String, Map<String, Object>>> requests) {

        listAvailable();

        List<CompletableFuture<String>> futures = new ArrayList<>();

       // CollectorEngine engine = new CollectorEngine(registry);
        

        for (Map<String, Map<String, Object>> request : requests) {

            String collectorName = request.keySet().iterator().next();
            Map<String, Object> parameters = request.get(collectorName);          

            CompletableFuture<String> future = registry.get(collectorName)
                .map(collector -> {

                    ServiceModel serviceRequest = new ServiceModel(collectorName, parameters);

                    return engine.executeAsync(serviceRequest)
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


    public List<String> activateServicesSync(List<Map<String, Map<String, Object>>> requests) {
        listAvailable();
        List<ServiceModel> serviceRequests = new ArrayList<>();
        AtomicReference<String> resultsJson = new AtomicReference<>("[]");

        // Process requests and separate collectors from errors
        requests.forEach(request -> {
            String collectorName = request.entrySet().iterator().next().getKey();
            Map<String, Object> parameters = request.get(collectorName);

            

            registry.get(collectorName).ifPresentOrElse((collector -> {
                serviceRequests.add(new ServiceModel(collectorName, parameters));
            }), () -> {
                return;
            });
        });

        // Execute valid collectors
        //CollectorEngine engine = new CollectorEngine(registry);
        Map<String, Future<CollectorResult>> futures = engine.executeAllSync(serviceRequests);

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
        //engine.shutdown();

        return List.of(resultsJson.get());
    } 



    // Utils

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


    public static ServiceManager begin() {
        return new ServiceManager();
    }

    public void end() {
        engine.shutdown();
    }

}
