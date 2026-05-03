# CollectorFramework

A Java-based pluggable framework for collecting system and hardware information. Collectors are discovered at runtime via Java's **Service Provider Interface (SPI)**, making it easy to add new data sources without modifying core code.

## Architecture

```
Shell (entry point)
    |
CollectorRequestActivator (facade)
    |
CollectorRegistry (SPI-based discovery via ServiceLoader)
    |
CollectorEngine (thread-pool execution + reflective parameter injection)
    |
Collector implementations (CPU, test, ...)
    |
CollectorResult (JSON output envelope)
```

The framework is split into three Maven modules:

- **`core`** — Public API (`Collector` interface, `CollectorResult`), SPI registry (`CollectorRegistry`), and execution engine (`CollectorEngine`). No runtime dependencies beyond SLF4J and json-simple.
- **`collectors`** — Collector implementations. Currently ships with a hardware CPU collector (using OSHI) and a generic test collector. Each is annotated with `@AutoService(Collector.class)` for automatic SPI registration.
- **`shell`** — Executable entry point. Assembles all modules into a standalone uber JAR.

## Key Concepts

### Collector Interface

Every data source implements the `Collector` interface:

```java
public interface Collector {
    String getName();
    CollectorResult collect();
    Map<String, Class<?>> getAcceptedParameters();
    boolean isAvailable();
}
```

### Annotation-Driven Metadata

Use `@CollectorMetadata` to declare a collector's name, description, tags, and thread safety:

```java
@CollectorMetadata(name = "hardware.cpu", tags = {"hardware", "realtime"})
public class CollectorCPU implements Collector { ... }
```

### SPI Discovery (Service Provider Interface)

Collectors are discovered at runtime via `ServiceLoader<Collector>`. The `@AutoService` annotation processor generates the required `META-INF/services/` entries at compile time, so no manual registration is needed.

### Reflective Parameter Injection

`CollectorEngine` inspects `getAcceptedParameters()` and injects values via reflection before each `collect()` call, enabling parameterized collector execution without a rigid configuration schema.

### Parallel Execution

`CollectorEngine.executeAll()` runs multiple collectors concurrently using a cached thread pool.

## Quick Start

```bash
./build.sh           # Build everything
./run.sh             # Run the application
./rebuild.sh         # Clean + build + run (all-in-one)
```

Or with Make:

```bash
make build
make run
make rebuild
make test
make dev             # Run from source (faster feedback)
```

## Building from Source

Requirements: Java 17+, Maven 3.8+

```bash
# Full build
mvn clean install -DskipTests

# Run from source (development)
mvn -f shell/pom.xml -am -q exec:java

# Build standalone JAR
mvn -f shell/pom.xml -am -q package -DskipTests
java -jar shell/target/collector-app.jar
```

## Output Locations

| Artifact | Path |
|----------|------|
| Core library | `core/target/core-1.0-SNAPSHOT.jar` |
| Collectors | `collectors/target/collectors-1.0-SNAPSHOT.jar` |
| Standalone app | `shell/target/collector-app.jar` |

## Adding a New Collector

1. Create a class implementing `Collector`
2. Annotate with `@AutoService(Collector.class)` and `@CollectorMetadata`
3. Place it in the `collectors` module (or a separate module depending on `core`)
4. Rebuild — the SPI descriptor is generated automatically

No configuration files, no registry edits, no core code changes.

## Dependencies

- **Java 17** — Language and runtime target
- **json-simple** — JSON serialization for collector results
- **SLF4J** — Logging facade
- **AutoService** — Compile-time SPI descriptor generation
- **OSHI** — Hardware information (CPU data)

## Documentation

- [BUILD.md](BUILD.md) — Detailed build and run instructions
- [QUICK_START.md](QUICK_START.md) — Command reference card
- [AUTOMATION.md](AUTOMATION.md) — Build automation overview
- `Makefile` — Run `make help` for all available targets