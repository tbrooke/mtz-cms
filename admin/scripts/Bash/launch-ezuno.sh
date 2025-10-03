#!/bin/bash
# Ezuno launcher script with Java 17

export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java version:"
java -version

echo "Starting Ezuno with Java 17..."

# Update this path to point to your Ezuno installation
# Example: java -jar /path/to/ezuno.jar
echo "Please update this script with the path to your Ezuno jar file"
echo "Then run: java -jar /path/to/your/ezuno.jar"