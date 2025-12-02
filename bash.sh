#!/bin/bash
set -e

echo "===================================================="
echo "🔍 Java Communicator — Build & Diagnostic Tool"
echo "===================================================="

# -------------------------------------------
# Detect OS (for jpackage/platform conditions)
# -------------------------------------------
OS=$(uname -s)
echo "➡ OS detected: $OS"

# -------------------------------------------
# Check JAVA_HOME & Java version
# -------------------------------------------
if [ -z "$JAVA_HOME" ]; then
  echo "❌ JAVA_HOME is not set!"
  exit 1
fi
echo "➡ JAVA_HOME = $JAVA_HOME"

JAVA_VER=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)
echo "➡ Java version: $JAVA_VER"
echo

# -------------------------------------------
# Set JavaFX JMOD path
# -------------------------------------------
if [ -z "$JFX_JMODS" ]; then
    export JFX_JMODS="$HOME/javafx-jmods-24"
fi

echo "➡ Checking JMOD path: $JFX_JMODS"
if [ ! -d "$JFX_JMODS" ]; then
  echo "❌ JavaFX JMOD folder not found!"
  ls -1 "$HOME/javafx-jmods" || true
  exit 1
fi
echo "✔ JavaFX JMODs found."
echo

# -------------------------------------------
# Paths
# -------------------------------------------
CORE_DIR="$HOME/Documents/Java-Core-Communicator-SWE/java"
FRONT_DIR="$HOME/Downloads/Java-front-Communicator-SWE/java"
MODULE="module-ux"
TARGET="$FRONT_DIR/$MODULE/target"
RUNTIME_IMAGE="$TARGET/runtime-test"

echo "➡ CORE path:  $CORE_DIR"
echo "➡ FRONT path: $FRONT_DIR"
echo

# -------------------------------------------
# Build CORE JAR
# -------------------------------------------
echo "=============================="
echo "🔥 Building CORE"
echo "=============================="
cd "$CORE_DIR"
mvn -q -pl module-app -am package -Dmaven.test.skip=true ||
  { echo "❌ Core build failed"; exit 1; }

CORE_JAR="$CORE_DIR/module-app/target/module-app-1.0-SNAPSHOT.jar"
[ -f "$CORE_JAR" ] || { echo "❌ Core JAR missing"; exit 1; }
echo "✔ Core JAR exists: $CORE_JAR"
echo

# Copy backend next to UI JAR
mkdir -p "$TARGET"
cp "$CORE_JAR" "$TARGET/core-backend.jar"
echo "✔ Copied backend: core-backend.jar"
echo

# -------------------------------------------
# Build FRONT JAR
# -------------------------------------------
echo "=============================="
echo "🎨 Building FRONT (UI)"
echo "=============================="
cd "$FRONT_DIR"
mvn -q -pl "$MODULE" -am package -DskipTests ||
  { echo "❌ Front build failed"; exit 1; }

UI_JAR="$TARGET/module-ux-1.0-SNAPSHOT.jar"
[ -f "$UI_JAR" ] || { echo "❌ UI JAR missing"; exit 1; }
echo "✔ UI JAR exists: $UI_JAR"
echo

# -------------------------------------------
# Create custom runtime
# -------------------------------------------
echo "=============================="
echo "🧱 Creating Java Runtime (JLINK)"
echo "=============================="
cd "$TARGET"
rm -rf "$RUNTIME_IMAGE"

"$JAVA_HOME/bin/jlink" \
  --module-path "$JAVA_HOME/jmods:$JFX_JMODS" \
  --add-modules java.base,java.desktop,java.datatransfer,java.net.http,javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.swing,jdk.crypto.ec \
  --strip-debug \
  --compress=2 \
  --no-header-files \
  --no-man-pages \
  --output "$RUNTIME_IMAGE" ||
  { echo "❌ jlink failed"; exit 1; }

echo "✔ Runtime image created: $RUNTIME_IMAGE"
echo

# -------------------------------------------
# Verify JavaFX included
# -------------------------------------------
echo "➡ Checking runtime modules..."
"$RUNTIME_IMAGE/bin/java" --list-modules | grep "javafx" ||
  { echo "❌ JavaFX modules missing in runtime"; exit 1; }
echo "✔ JavaFX present in runtime"
echo

# -------------------------------------------
# Test-launch application
# -------------------------------------------
echo "=============================="
echo "🚀 Running app (with backend)"
echo "=============================="

("$RUNTIME_IMAGE/bin/java" -jar "$UI_JAR" > test_run.log 2>&1 &) # run silently in background
PID=$!
sleep 5

if ! ps -p $PID > /dev/null; then
  echo "❌ App failed to launch. Logs below:"
  cat test_run.log
  exit 1
fi

echo "✔ Frontend launched successfully."
echo

# -------------------------------------------
# Check backend logs
# -------------------------------------------
echo "➡ Checking backend logs..."
LOG_FILE=$(ls backend*.log 2>/dev/null | head -n1 || true)

if [ -z "$LOG_FILE" ]; then
  echo "❌ No backend logs found (backend thread not started?)"
else
  echo "✔ Backend log found: $LOG_FILE"
  tail -n 10 "$LOG_FILE"
fi

kill $PID 2>/dev/null || true

echo
echo "===================================================="
echo "🎯 FINAL RESULT: COMPLETED DIAGNOSTICS"
echo "===================================================="
