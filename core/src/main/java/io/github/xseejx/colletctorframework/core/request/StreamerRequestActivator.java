package io.github.xseejx.colletctorframework.core.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.github.xseejx.colletctorframework.core.engine.stream.StopCondition;
import io.github.xseejx.colletctorframework.core.engine.stream.StreamSpec;
import io.github.xseejx.colletctorframework.core.engine.stream.StreamTrigger;
import io.github.xseejx.colletctorframework.core.engine.stream.StreamerEngine;

public class StreamerRequestActivator {
    private final StreamerEngine engine;


    public StreamerRequestActivator(CollectorRequestActivator activator) {
        this.engine = new StreamerEngine(activator, 4);
    }


    
    public UUID heartbeat(String collectorName,
                          Map<String, Object> params,
                          Consumer<String> onEmit,
                          StopCondition stopCondition) {

        return engine.start(StreamSpec.builder(collectorName)
            .parameters(params)
            .trigger(StreamTrigger.everySeconds(1))
            .onEmit(onEmit)
            .stopCondition(stopCondition)
            .build());
    }



    public UUID stream(StreamSpec spec) {
        return engine.start(spec);
    }

    public void stop(UUID id)        { engine.stop(id); }
    public void stopAll()            { engine.stopAll(); }
    public Set<UUID> listActive()    { return engine.listActive(); }


    public List<String> drain(UUID id) {
        return engine.get(id).map(handle -> {
            List<String> results = new ArrayList<>();
            handle.getQueue().drainTo(results);
            return results;
        }).orElse(List.of());
    }


    public Optional<String> poll(UUID id, long timeout, TimeUnit unit) throws InterruptedException {
        return engine.get(id)
            .map(handle -> {
                try { return handle.getQueue().poll(timeout, unit); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); return null; }
            });
    }

    public void shutdown() { engine.shutdown(); }


}
