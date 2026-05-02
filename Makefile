.PHONY: build run rebuild clean test install dev help

help:
	@echo "CollectorFramework - Available targets:"
	@echo ""
	@echo "  make build       - Build entire project (compiles all modules)"
	@echo "  make run         - Run the application with full dependencies"
	@echo "  make rebuild     - Clean build and run (full rebuild)"
	@echo "  make clean       - Clean all build artifacts"
	@echo "  make test        - Run all unit tests"
	@echo "  make install     - Install dependencies in Maven local repo"
	@echo "  make dev         - Run with exec plugin (for development)"
	@echo "  make help        - Show this help message"
	@echo ""

build:
	@echo "Building CollectorFramework..."
	mvn -q clean install -DskipTests
	@echo "✓ Build completed successfully!"

run: build
	@echo "Running CollectorFramework..."
	java -jar shell/target/collector-app.jar

rebuild: clean build run

clean:
	@echo "Cleaning all build artifacts..."
	mvn -q clean
	@echo "✓ Clean completed"

test:
	@echo "Running tests..."
	mvn -q test

install:
	@echo "Installing dependencies..."
	mvn -q install -DskipTests

dev: build
	@echo "Running with Maven exec plugin (development mode)..."
	mvn -f shell/pom.xml -q exec:java

.PHONY: clean-cache
clean-cache:
	@echo "Clearing Maven cache for this project..."
	rm -rf ~/.m2/repository/io/github/xseejx/colletctorframework/
	@echo "✓ Cache cleared"

.PHONY: full-rebuild
full-rebuild: clean-cache build run
