#!/bin/bash

# Run the CollectorFramework application with automatic dependency resolution
set -e

# Check if the app JAR exists
if [ ! -f "shell/target/collector-app.jar" ]; then
    echo "App JAR not found. Building first..."
    ./build.sh
fi

echo "Running CollectorFramework..."
java -jar shell/target/collector-app.jar
