#!/bin/bash

# Build the entire CollectorFramework project
set -e

echo "Building CollectorFramework..."
mvn -q clean install -DskipTests

echo "✓ Build completed successfully!"
echo ""
echo "Available JARs:"
echo "  - Core module:      core/target/core-1.0-SNAPSHOT.jar"
echo "  - Collectors:       collectors/target/collectors-1.0-SNAPSHOT.jar"
echo "  - Shell (app):      shell/target/collector-app.jar"
echo ""
echo "Run with: ./run.sh"
