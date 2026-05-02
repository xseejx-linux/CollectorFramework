# Quick Reference - Build Commands

## Start Here

```bash
./build.sh          # Build everything
./run.sh            # Run the app
./rebuild.sh        # Clean + build + run (all-in-one)
```

## ake Targets

```bash
make build          # Build all modules
make run            # Run application  
make rebuild        # Full clean rebuild
make test           # Run unit tests
make clean          # Delete build artifacts
make dev            # Run from source
make clean-cache    # Clear Maven cache
make help           # Show all targets
```

## Maven Direct Commands

```bash
# Build
mvn -q clean install -DskipTests

# Run from source
mvn -f shell/pom.xml -am -q exec:java

# Build JAR only
mvn -f shell/pom.xml -am -q package -DskipTests

# Run JAR
java -jar shell/target/collector-app.jar

# Run tests
mvn -q test
```

## VS Code (IDE Integration)

1. Press `Ctrl+Shift+P` (Linux/Win) or `Cmd+Shift+P` (Mac)
2. Type `Tasks: Run Task`
3. Choose:
   - Build Project
   - Run Application
   - Rebuild & Run
   - Run Tests
   - Clean Build
   - Clear Maven Cache

Or use keyboard shortcut for default:
- `Ctrl+Shift+B` = Build Project

## Common Workflows

### Fresh Start
```bash
make clean-cache && make rebuild
```

### Quick Development Cycle
```bash
make build && make dev
```

### Run Tests + Build + Run
```bash
make test && make run
```

### Only Run (assume already built)
```bash
java -jar shell/target/collector-app.jar
```

## Output Locations

| JAR | Location |
|-----|----------|
| Core | `core/target/core-1.0-SNAPSHOT.jar` |
| Collectors | `collectors/target/collectors-1.0-SNAPSHOT.jar` |
| App (standalone) | `shell/target/collector-app.jar` ← Use this! |

## Tips

- All scripts are **idempotent** - safe to run multiple times
- Use `./run.sh` for production (single JAR, all deps included)
- Use `make dev` during development (faster feedback)
- Use `make test` before committing
- Scripts work from any directory in the project

## Full Documentation

See `BUILD.md` or `AUTOMATION.md` for detailed explanations.
