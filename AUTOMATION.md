# Build Automation Setup - Summary

CollectorFramework project has **4 ways to build and run** with automated dependency and classpath management:

## ⚡ Quick Reference

| Method | Command | Best For |
|--------|---------|----------|
| **Shell Scripts** | `./build.sh` → `./run.sh` | Simple, no dependencies (works everywhere) |
| **Make** | `make build` → `make run` | Power users, multiple targets |
| **Maven (Direct)** | `mvn -f shell/pom.xml -q exec:java` | Development, running from source |
| **VS Code Tasks** | `Ctrl+Shift+P` → `Tasks: Run Task` | IDE integration, one-click builds |

---

## 1. Shell Scripts

**Files created:**
- `build.sh` - Compiles and packages everything
- `run.sh` - Runs the app JAR (auto-builds if needed)
- `rebuild.sh` - Clean build and run

**Usage:**
```bash
./build.sh        # Full build, 1 command
./run.sh          # Run with dependencies resolved
./rebuild.sh      # Clean everything, rebuild, run
```

**What it does:**
- Compiles core → collectors → shell modules
- Packages SPI service descriptors correctly
- Creates standalone JAR: `shell/target/collector-app.jar`
- Handles all Maven phases automatically
- No manual classpath needed

---

## 2. Make Commands

**Usage:**
```bash
make help         # Show all targets
make build        # Same as ./build.sh
make run          # Same as ./run.sh
make rebuild      # Same as ./rebuild.sh
make test         # Run all unit tests
make clean        # Remove build artifacts
make dev          # Run from source (exec:java)
make install      # Install to Maven repo
make clean-cache  # Clear project from Maven cache
make full-rebuild # Clean cache + rebuild
```

**Example workflow:**
```bash
make clean-cache && make rebuild
```

---

## 3. Maven Commands

**Build only:**
```bash
mvn -q clean install -DskipTests
```

**Run with exec plugin (from source):**
```bash
mvn -f shell/pom.xml -am -q exec:java
```

**Build and create JAR:**
```bash
mvn -f shell/pom.xml -am -q package -DskipTests
```

**Run the standalone JAR:**
```bash
java -jar shell/target/collector-app.jar
```

---

## 4. VS Code Tasks

**Access via:**
- `Ctrl+Shift+P` (Windows/Linux) or `Cmd+Shift+P` (Mac)
- Type: `Tasks: Run Task`
- Select from:
  - `Build Project` (Ctrl+Shift+B)
  - `Run Application`
  - `Rebuild & Run`
  - `Run Tests`
  - `Clean Build`
  - `Clear Maven Cache`

**Configuration file:** `.vscode/tasks.json`

---

## How Dependencies Are Managed

The **Maven Assembly Plugin** is now configured to:

1. **Gather all dependencies** - Collects core, collectors, and all transitive deps (OSHI, JSON, SLF4J, etc.)

2. **Merge resources** - Preserves Java SPI service descriptors from each module

3. **Create uber JAR** - Packages everything into `shell/target/collector-app.jar`

4. **Set main class** - Automatically finds entry point: `io.github.xseejx.colletctorframework.shell.App`

Result: Single `java -jar collector-app.jar` command runs the entire stack.

---

## File Structure After Build

```
CollectorFramework/
├── build.sh                           # Script: Full build
├── run.sh                             # Script: Run app
├── rebuild.sh                         # Script: Clean build + run
├── Makefile                           # Make automation targets
├── BUILD.md                           # (Reference doc for building)
├── .vscode/
│   └── tasks.json                     # VS Code task definitions
├── core/
│   └── target/
│       └── core-1.0-SNAPSHOT.jar      # Core API + Registry
├── collectors/
│   └── target/
│       └── collectors-1.0-SNAPSHOT.jar # Hardware collectors
└── shell/
    └── target/
        └── collector-app.jar          # Standalone executable
```

---

## Example Session

```bash
# Clone/navigate to project
cd CollectorFramework

# First time setup - build everything
./build.sh

# Run it
./run.sh

# Output:
# Waiting for collector result...
# Loaded: class io.github.xseejx.colletctorframework.collectors.hardware.CollectorCPU
# Collector result: {"data":{"model":"Test"},...}
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "collector not found" | `make clean-cache && make rebuild` |
| Tests failing | `make test` to run with full output |
| Want to run from source | `make dev` (no JAR needed) |
| Classpath errors | Use `./run.sh` (jar handles all dependencies) |

---

## ToDO

1. **Try different methods:**
   ```bash
   make run              # Make version
   mvn exec:java         # Maven version  
   ./run.sh              # Shell script version
   ```

2. **Set up IDE shortcut:**
   - In VS Code: Bind `Ctrl+Shift+B` to "Build Project" task
   - In VS Code: Bind `F5` to "Run Application" task

3. **Add CI/CD:**
   - Use `make build` in GitHub Actions
   - Run `./build.sh` in Docker/containers

---
