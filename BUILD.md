# CollectorFramework - Build & Run Automation

This document explains how to build and run the CollectorFramework project using automated scripts.

## Quick Start

### Option 1: Shell Scripts (Recommended)

```bash
# Build the entire project
./build.sh

# Run the application (auto-builds if needed)
./run.sh

# Clean build and run
./rebuild.sh
```

### Option 2: Make Commands

```bash
# Show all available targets
make help

# Build project
make build

# Run application
make run

# Full rebuild
make rebuild

# Run tests
make test

# Clean artifacts
make clean

# Clear Maven cache (if you have stale dependencies)
make clean-cache

# Full rebuild with cache cleared
make full-rebuild
```

### Option 3: Maven Directly

```bash
# Build entire project
mvn -q clean install -DskipTests

# Run application with exec plugin
mvn -f shell/pom.xml -am -q exec:java

# Generate standalone JAR with all dependencies
mvn -f shell/pom.xml -am -q package -DskipTests
# Then run: java -jar shell/target/collector-app.jar
```

### Option 4: VS Code Tasks

Open the Command Palette (`Ctrl+Shift+P` or `Cmd+Shift+P`) and type `Tasks: Run Task` to see:

- `Build Project` - Compile everything
- `Run Application` - Run the app with dependencies
- `Rebuild & Run` - Full clean rebuild and execution
- `Run Tests` - Execute all unit tests
- `Clean Build` - Clean build artifacts
- `Clear Maven Cache` - Remove stale project dependencies from Maven cache

## Output Locations

After building, you'll find:

| File | Location | Description |
|------|----------|-------------|
| Core JAR | `core/target/core-1.0-SNAPSHOT.jar` | Core framework API & registry |
| Collectors JAR | `collectors/target/collectors-1.0-SNAPSHOT.jar` | Hardware collector implementations |
| App JAR (standalone) | `shell/target/collector-app.jar` | Executable application with all dependencies |

## What Each Script Does

### build.sh
- Cleans previous builds
- Compiles all modules (core → collectors → shell)
- Packages everything into JARs
- Installs to local Maven repository
- Resources are copied correctly (service descriptors)

### run.sh
- Checks if the app JAR exists
- Automatically runs build.sh if needed
- Executes the standalone JAR with `java -jar`
- No manual classpath management needed

### rebuild.sh
- Runs build.sh followed by run.sh
- Equivalent to `make rebuild`

## How Dependencies Are Managed

1. **Service Loader Configuration**: The project uses Java SPI (Service Provider Interface) to discover collectors at runtime
   - Service names are registered in `collectors/src/main/resources/META-INF/services/`
   - When AppJAR runs, it automatically finds and loads collectors

2. **Maven Assembly Plugin**: Creates a fat/uber JAR with all dependencies
   - All JARs and resources are packaged into `shell/target/collector-app.jar`
   - Single command to run: `java -jar shell/target/collector-app.jar`
   - No need to manually specify classpaths

3. **Exec Plugin**: For development, allows running from source without building JARs
   - Use `make dev` or `mvn -f shell/pom.xml exec:java`
   - Faster feedback loop during development

## Troubleshooting

### "Command not found" or no output
```bash
# Clear Maven cache and rebuild
make clean-cache
make rebuild
```

### Tests are failing
```bash
# Run with full test suite
make test
```

### Classpath issues
```bash
# Verify all modules built correctly
mvn -q clean verify

# Or use the standalone JAR
java -jar shell/target/collector-app.jar
```

## Environment

This setup is tested with:
- Maven 3.8.0+
- Java 17+
- Linux/Unix/macOS

All commands work directly from the project root directory.
