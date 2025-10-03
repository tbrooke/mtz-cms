#!/bin/bash
# Ezuno Launcher Script - Use Java 17

echo "🚀 Starting Ezuno with Java 17..."

# Set Java 17 environment
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"

# Show Java version
echo "Using Java version:"
java -version
echo ""

# Launch Ezuno
cd /Applications/ezuno.app/Contents/Java
echo "Launching Ezuno..."
java -jar ezuno-desktop-distribution-standard-5.10.19-shaded.jar

echo "Ezuno stopped."