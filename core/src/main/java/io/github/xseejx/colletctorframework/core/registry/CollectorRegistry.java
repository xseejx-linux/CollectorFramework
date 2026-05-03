package io.github.xseejx.colletctorframework.core.registry;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.github.xseejx.colletctorframework.core.api.CollectorMetadata;
import io.github.xseejx.colletctorframework.core.api.Collector;

public class CollectorRegistry {
    private final Map<String, Collector> collectors = new ConcurrentHashMap<>();

    public void register(Collector collector) {
        CollectorMetadata meta = collector.getClass()
            .getAnnotation(CollectorMetadata.class);

        String name = (meta != null) ? meta.name() : collector.getName();

        collectors.put(name, collector);
    }

    public Optional<Collector> get(String name) {
        return Optional.ofNullable(collectors.get(name));
    }

    public Set<String> listAvailable() {
        return collectors.entrySet().stream()
            .filter(e -> e.getValue().isAvailable())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }


    public void discoverAll() {

        ServiceLoader<Collector> loader = ServiceLoader.load(Collector.class);

        for (Collector c : loader) {
            try {
                //System.out.println("Loaded: " + c.getClass());
                register(c);
            } catch (ServiceConfigurationError e) {
                //System.err.println("Failed to load collector provider: " + e.getMessage());
            }
        }
    }

}