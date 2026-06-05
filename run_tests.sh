#!/bin/bash
# Script pour compiler et lancer les tests JUnit du projet.
# Les resultats sont sauvegardes dans test-reports/.
# Usage : ./run_tests.sh

set -e
cd "$(dirname "$0")"

JAVA_HOME_LOCAL="/Users/kies/Library/Java/JavaVirtualMachines/openjdk-26.0.1/Contents/Home"
JAVAFX_LIB="/Users/kies/Downloads/javafx-sdk-26.0.1-arm64/lib"
JUNIT_JAR="lib/junit/junit-platform-console-standalone-1.11.3.jar"
BUILD_DIR="/tmp/minicraft-build"
REPORTS_DIR="test-reports"

mkdir -p "$BUILD_DIR" "$REPORTS_DIR"

# horodatage pour la sortie texte (les xml sont ecrases par junit a chaque fois)
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$REPORTS_DIR/results_$TIMESTAMP.txt"

echo "==> Compilation..."
"$JAVA_HOME_LOCAL/bin/javac" \
    --module-path "$JAVAFX_LIB" \
    --add-modules javafx.controls,javafx.fxml,javafx.media \
    -cp "$JUNIT_JAR" \
    -d "$BUILD_DIR" \
    $(find src tests -name "*.java")

echo "==> Lancement des tests (sortie sauvegardee dans $LOG_FILE)..."
"$JAVA_HOME_LOCAL/bin/java" \
    --module-path "$JAVAFX_LIB" \
    --add-modules javafx.controls,javafx.fxml,javafx.media \
    --enable-native-access=javafx.graphics,javafx.media \
    -jar "$JUNIT_JAR" execute \
    --class-path "$BUILD_DIR" \
    --scan-class-path \
    --reports-dir "$REPORTS_DIR" \
    --disable-ansi-colors \
    --details=tree \
    2>&1 | tee "$LOG_FILE"

echo ""
echo "==> Rapports XML  : $REPORTS_DIR/TEST-junit-jupiter.xml"
echo "==> Sortie texte  : $LOG_FILE"
