package io.github.xseejx.collectorframework.internal.registry;

// IMPORTS
import java.util.Map;
import java.util.Optional;
// IMPORTS
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
// IMPORTS
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
// IMPORTS API
import io.github.xseejx.collectorframework.api.CollectorMetadata;
import io.github.xseejx.collectorframework.api.Collector;

/**
 * 
 */
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
                register(c);
            } catch (ServiceConfigurationError e) {
            }
        }
    }

}