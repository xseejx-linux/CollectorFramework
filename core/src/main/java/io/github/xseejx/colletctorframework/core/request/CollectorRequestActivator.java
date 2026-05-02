package io.github.xseejx.colletctorframework.core.request;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;

import io.github.xseejx.colletctorframework.core.api.CollectorResult;
import io.github.xseejx.colletctorframework.core.engine.CollectorEngine;
import io.github.xseejx.colletctorframework.core.registry.CollectorRegistry;

public class CollectorRequestActivator {


    public String activateRequest(String collectorName, Map<String, Object> parameters) {
        CollectorRegistry registry = new CollectorRegistry();


        registry.discoverAll();

       //System.out.println("Available collectors: " + registry.listAvailable());
        
        CollectorRequest request = new CollectorRequest(collectorName, parameters);
        AtomicReference<String> jsonResponse = new AtomicReference<>("{}");
        
        registry.get(collectorName).ifPresent(collector -> {
            CollectorEngine engine = new CollectorEngine(registry);
            Future<CollectorResult> result = engine.execute(request);
            try {
                CollectorResult res = result.get();
                jsonResponse.set(res.getResult().toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            engine.shutdown();
        });

        return jsonResponse.get(); 
    }

    public void activateRequests(List<CollectorRequest> requests) {
        // TODO: implement batch execution
    }
}
