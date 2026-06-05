#!/bin/bash
# Script pour compiler et lancer les tests JUnit du projet.
# Usage : ./run_tests.sh

set -e
cd "$(dirname "$0")"

JAVA_HOME_LOCAL="/Users/kies/Library/Java/JavaVirtualMachines/openjdk-26.0.1/Contents/Home"
JAVAFX_LIB="/Users/kies/Downloads/javafx-sdk-26.0.1-arm64/lib"
JUNIT_JAR="lib/junit/junit-platform-console-standalone-1.11.3.jar"
BUILD_DIR="/tmp/minicraft-build"

mkdir -p "$BUILD_DIR"

echo "==> Compilation..."
"$JAVA_HOME_LOCAL/bin/javac" \
    --module-path "$JAVAFX_LIB" \
    --add-modules javafx.controls,javafx.fxml,javafx.media \
    -cp "$JUNIT_JAR" \
    -d "$BUILD_DIR" \
    $(find src tests -name "*.java")

echo "==> Lancement des tests..."
"$JAVA_HOME_LOCAL/bin/java" \
    --module-path "$JAVAFX_LIB" \
    --add-modules javafx.controls,javafx.fxml,javafx.media \
    --enable-native-access=javafx.graphics,javafx.media \
    -jar "$JUNIT_JAR" execute \
    --class-path "$BUILD_DIR" \
    --scan-class-path
