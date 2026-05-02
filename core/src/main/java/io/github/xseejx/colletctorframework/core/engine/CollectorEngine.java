package io.github.xseejx.colletctorframework.core.engine;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.xseejx.colletctorframework.core.registry.CollectorRegistry;
import io.github.xseejx.colletctorframework.core.request.CollectorRequest;
import io.github.xseejx.colletctorframework.core.api.Collector;
import io.github.xseejx.colletctorframework.core.api.CollectorResult;


public class CollectorEngine {
    private static final Logger logger = LoggerFactory.getLogger(CollectorEngine.class);

    private final CollectorRegistry registry;
    private final ExecutorService threadPool;

    public CollectorEngine(CollectorRegistry registry) {
        this.registry   = registry;
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Execute a single named collector, with optional parameter injection.
     */
    public Future<CollectorResult> execute(CollectorRequest request) {
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
     * Execute multiple collectors in parallel, return all results.
     */
    public Map<String, Future<CollectorResult>> executeAll(List<CollectorRequest> requests) {
        Map<String, Future<CollectorResult>> futures = new LinkedHashMap<>();
        for (CollectorRequest req : requests) {
            futures.put(req.getCollectorName(), execute(req));
        }
        return futures;
    }

    /**
     * Reflectively set fields on a collector before invocation.
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

    public void shutdown() {
        threadPool.shutdown();
    }
}
