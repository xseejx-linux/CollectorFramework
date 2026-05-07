package io.github.xseejx.collectorframework.engine.internal.registry;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.github.xseejx.collectorframework.api.DispatcherMetadata;
import io.github.xseejx.collectorframework.api.ResultDispatcher;

public class ResultDispatcherRegistry {
    private final Map<String, ResultDispatcher> dispatchers = new ConcurrentHashMap<>();

    public void register(ResultDispatcher dispatcher) {
        DispatcherMetadata meta = dispatcher.getClass().getAnnotation(DispatcherMetadata.class);
        String name = (meta != null) ? meta.name() : dispatcher.getClass().getSimpleName().toLowerCase();
        dispatchers.put(name.toLowerCase(), dispatcher);
    }

    public Optional<ResultDispatcher> get(String name) {
        if (name == null || name.isBlank()) {
            return getDefaultOptional();
        }
        return Optional.ofNullable(dispatchers.get(name.toLowerCase()));
    }

    public Optional<ResultDispatcher> getDefaultOptional() {
        return Optional.ofNullable(dispatchers.get("console"))
            .or(() -> dispatchers.values().stream().findFirst());
    }

    public ResultDispatcher getDefault() {
        return getDefaultOptional().orElseThrow(() -> new IllegalStateException("No ResultDispatcher implementations found"));
    }

    public Set<String> listAvailable() {
        return dispatchers.keySet().stream().sorted().collect(Collectors.toSet());
    }

    public void discoverAll() {
        ServiceLoader<ResultDispatcher> loader = ServiceLoader.load(ResultDispatcher.class);
        for (ResultDispatcher dispatcher : loader) {
            System.out.println("Discovered ResultDispatcher: " + dispatcher.getClass().getName());
            try {
                register(dispatcher);
            } catch (ServiceConfigurationError ignored) {
            }
        }
    }
}
