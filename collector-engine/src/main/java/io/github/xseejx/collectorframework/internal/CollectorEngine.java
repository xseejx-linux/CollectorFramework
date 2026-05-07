package io.github.xseejx.collectorframework.internal;

// IMPORTS
import java.lang.reflect.Field;
// IMPORTS
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
// IMPORTS
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
// IMPORTS
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// IMPORTS INTERNAL
import io.github.xseejx.collectorframework.internal.registry.CollectorRegistry;
import io.github.xseejx.collectorframework.ServiceModel;
// IMPORTS API
import io.github.xseejx.collectorframework.Collector;
import io.github.xseejx.collectorframework.CollectorResult;

/**
 * 
 */
public class CollectorEngine {
    private static final Logger logger = LoggerFactory.getLogger(CollectorEngine.class);

    private final CollectorRegistry registry;
    private final ExecutorService threadPool;

    /**
     * Constructor for CollectorEngine, takes a CollectorRegistry to look up collectors.
     * @param registry
     */
    public CollectorEngine(CollectorRegistry registry) {
        this.registry   = registry;
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Synchronously execute a single named collector, with optional parameter injection.
     * @param request
     * @return
     */ 
    public Future<CollectorResult> executeSync(ServiceModel request) {
        return threadPool.submit(() -> {
            Collector collector = registry.get(request.getCollectorName())
                .orElseThrow(() -> new CollectorNotFoundException(request.getCollectorName()));

            if (!collector.isAvailable()) {
                return CollectorResult.failure(request.getCollectorName(), new JSONObject());
            }

            injectParameters(collector, request.getParameters());

            
            return collector.collect();
        });
    }

    /**
     * Asynchronously execute a single named collector, with optional parameter injection.
     * @param request
     * @return CompletableFuture<CollectorResult>
     */
    public CompletableFuture<CollectorResult> executeAsync(ServiceModel request) {
        return CompletableFuture.supplyAsync(() -> {
            Collector collector = registry.get(request.getCollectorName())
                .orElseThrow(() -> new CollectorNotFoundException(request.getCollectorName()));

            if (!collector.isAvailable()) {
                return CollectorResult.failure(request.getCollectorName(), new JSONObject());
            }

            injectParameters(collector, request.getParameters());

            
            return collector.collect();
        });
    }

    /**
     * Execute multiple collectors in parallel, return all results.
     * @param requests
     */
    public Map<String, Future<CollectorResult>> executeAllSync(List<ServiceModel> requests) {
        Map<String, Future<CollectorResult>> futures = new LinkedHashMap<>();
        for (ServiceModel req : requests) {
            futures.put(req.getCollectorName(), executeSync(req));
        }
        return futures;
    }

    /**
     * Use reflection to inject parameters into collector fields before execution.
     * @param collector
     * @param params
     */
    private void injectParameters(Collector collector, Map<String, Object> params) {
        if (params == null || params.isEmpty()) return;

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            try {
                Field field = collector.getClass().getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(collector, entry.getValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // log and skip unknown params — never crash the engine
                logger.debug("Parameter '{}' not found on collector '{}', skipping injection", entry.getKey(), collector.getName());
            }
        }
    }

    /**
     * Shutdown the engine and its thread pool.
     */
    public void shutdown() {
        threadPool.shutdown();
    }   
}
