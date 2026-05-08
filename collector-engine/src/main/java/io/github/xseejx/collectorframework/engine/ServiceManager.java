package io.github.xseejx.collectorframework.engine;

// IMPORTS
import java.lang.annotation.Annotation;
// IMPORTS
import java.lang.reflect.Array;
import java.lang.reflect.Method;
// IMPORTS
import java.nio.file.Path;
// IMPORTS
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
// IMPORTS
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
// IMPORTS API
import io.github.xseejx.collectorframework.api.Collector;
import io.github.xseejx.collectorframework.api.CollectorMetadata;
import io.github.xseejx.collectorframework.api.CollectorMetadata.ParameterType;
import io.github.xseejx.collectorframework.api.CollectorResult;
// IMPORTS INTERNAL
import io.github.xseejx.collectorframework.engine.internal.CollectorEngine;
import io.github.xseejx.collectorframework.engine.internal.CollectorNotFoundException;
import io.github.xseejx.collectorframework.engine.internal.registry.CollectorRegistry;


/**
 * Class: ServiceManager
 * Class used for enabling a ServiceManager on which it can start on-requests execution in sync or async
 * 
 * TODO: As of this version it can be fully replaced by TaskManager
 */
public class ServiceManager {

    private final CollectorRegistry registry = new CollectorRegistry();
    private final CollectorEngine engine = new CollectorEngine(registry);

    /**
     * Activate a single collector synchronously, with optional parameters.
     * @param collectorName
     * @param parameters
     * @return
     */
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
        });
        return jsonResponse.get(); 
    }

    /**
     * Activate a single collector asynchronously, with optional parameters.
     * @param collectorName
     * @param parameters
     * @return
     */
    public CompletableFuture<String> activateServiceAsync(String collectorName, Map<String, Object> parameters) {
        listAvailable();

        ServiceModel request = new ServiceModel(collectorName, parameters);

        return engine.executeAsync(request)
            .thenApply(res -> res.getResult().toJSONString())
            .exceptionally(e -> "{\"error\": \"" + e.getMessage() + "\"}");
    }

    //TODO: must return a json array of results with collector name and result or error for each request
    public List<CompletableFuture<String>> activateServicesAsync(List<Map<String, Map<String, Object>>> requests) {
        listAvailable();
        List<CompletableFuture<String>> futures = new ArrayList<>();
     
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

    /**
     * Activate multiple collectors synchronously, with optional parameters.
     * @param requests
     * @return
     */
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
        return List.of(resultsJson.get());
    } 

    /**
     * Get metadata information about a collector, such as description and tags.
     * @param collectorName
     * @return
     */
    public List<String> getMetadata(String collectorName) {
        listAvailable();

        List<String> results = new ArrayList<>();
    
        registry.get(collectorName).ifPresent(collector -> {
            CollectorMetadata metadata =
                collector.getClass().getAnnotation(CollectorMetadata.class);
            if (metadata == null) {
                results.add("No metadata found");
                return;
            }
            for (Method method : metadata.annotationType().getDeclaredMethods()) {
                try {
                    Object value = method.invoke(metadata);
                    String formattedValue = formatValue(value);
                    results.add(method.getName() + ": " + formattedValue);
                } catch (Exception e) {
                    results.add(method.getName() + ": <error>");
                }
            }
        });

        return results;
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        Class<?> clazz = value.getClass();
        if (clazz.isArray()) {
            int length = Array.getLength(value);
            List<String> values = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                values.add(formatValue(Array.get(value, i)));
            }
            return "[" + String.join(", ", values) + "]";
        }
        if (value instanceof Annotation annotation) {
            List<String> parts = new ArrayList<>();
            for (Method method : annotation.annotationType().getDeclaredMethods()) {
                try {
                    Object nestedValue = method.invoke(annotation);
                    parts.add(method.getName() + "=" + formatValue(nestedValue));
                } catch (Exception e) {
                    parts.add(method.getName() + "=<error>");
                }
            }
            return "(" + String.join(", ", parts) + ")";
        }
        return String.valueOf(value);
    }

    /**
     * List all available collectors that can be executed.
     * @return
     */
    public List<String> listAvailable() {
        registry.discoverAll();
        return registry.listAvailable().stream().sorted().toList();
    }

    /**
     * Begin a service manager session.
     * @return
     */
    public static ServiceManager begin() {
        return new ServiceManager();
    }

    /**
     * End the service manager session, shutting down any resources.
     */
    public void end() {
        engine.shutdown();
    }

    /**
     * Get Collector Object.
     * To soon remove as it doesn't respect the project structure, but it jumps straight to the collector
     * @param collectorName
     * @return
     */
    @Deprecated
    public Collector getCollector(String collectorName) {
        listAvailable();        
        Collector collector = registry.get(collectorName)
                .orElseThrow(() -> new CollectorNotFoundException(collectorName));

        return collector;
    }

      /**
     * (Once getAcceptedParameters() is remove or replaced this function will too) yet not deprecated as it is used
     * Retrieves the list of internal parameters for a collector when the
     * {@code CollectorMetadata} annotation does not define any parameters.
     * <p>
     * This method checks if the annotation's {@code parameters()} array is
     * empty. If it is, the method calls {@link Collector#getAcceptedParameters()}
     * and converts each entry into a map containing the parameter's key,
     * type (as a {@link ParameterType} string), an empty default value, and
     * {@code required} set to {@code false}.
     *
     * @param collectorName the name of the collector to inspect
     * @return a list of parameter descriptions, or an empty list if the
     *         annotation already defines parameters or the collector is not found
     */
    public List<Map<String, Object>> getUnlistedParameters(String collectorName) {
        listAvailable();
        List<Map<String, Object>> result = new ArrayList<>();

        registry.get(collectorName).ifPresent(collector -> {
            CollectorMetadata metadata = collector.getClass().getAnnotation(CollectorMetadata.class);
            if (metadata != null && metadata.parameters().length == 0) {
                Map<String, Class<?>> accepted = collector.getAcceptedParameters();
                if (accepted != null) {
                    for (Map.Entry<String, Class<?>> entry : accepted.entrySet()) {
                        Map<String, Object> paramMap = new LinkedHashMap<>();
                        paramMap.put("key", entry.getKey());
                        paramMap.put("type", mapClassToParameterType(entry.getValue()).name());
                        paramMap.put("defaultValue", "");
                        paramMap.put("required", false);
                        result.add(paramMap);
                    }
                }
            }
        });

        return result;
    }

    /**
     * Maps a Java {@link Class} to the corresponding {@link ParameterType}.
     *
     * @param clazz the Java type to map
     * @return the matching {@code ParameterType}, or {@link ParameterType#UNKNOWN}
     *         if no direct mapping exists
     */
    private ParameterType mapClassToParameterType(Class<?> clazz) {
        if (clazz == String.class) return ParameterType.STRING;
        if (clazz == boolean.class || clazz == Boolean.class) return ParameterType.BOOLEAN;
        if (clazz == int.class || clazz == Integer.class) return ParameterType.INTEGER;
        if (clazz == float.class || clazz == Float.class) return ParameterType.FLOAT;
        if (clazz == long.class || clazz == Long.class) return ParameterType.LONG;
        if (clazz == Path.class) return ParameterType.PATH;
        if (clazz == byte[].class) return ParameterType.BINARY;
        return ParameterType.UNKNOWN;
    }

}
