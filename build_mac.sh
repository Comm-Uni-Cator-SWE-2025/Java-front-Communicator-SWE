#!/bin/bash
set -e

APP_NAME="VARTΛŁΛpp"
VERSION="1.0"

# ╔════════════════ DIRECTORY SETUP ════════════════╗
CORE_REPO="$HOME/Documents/Java-Core-Communicator-SWE"
FRONT_REPO="$(cd "$(dirname "$0")"; pwd)"

CORE_JAVA_DIR="$CORE_REPO/java"
FRONT_JAVA_DIR="$FRONT_REPO/java"
FRONT_MODULE="module-ux"

FRONT_TARGET="$FRONT_JAVA_DIR/$FRONT_MODULE/target"
FRONT_DIST="$FRONT_JAVA_DIR/$FRONT_MODULE/dist"

mkdir -p "$FRONT_DIST"

echo "➡ Backend: $CORE_JAVA_DIR"
echo "➡ Frontend: $FRONT_JAVA_DIR"
echo

# ╔════════════════ JAVA SETUP ════════════════╗
if [ -z "$JAVA_HOME" ]; then
  echo "❌ JAVA_HOME is not set!"
  echo "👉 Set to JDK 24 before running"
  exit 1
fi
echo "➡ Using JAVA_HOME: $JAVA_HOME"
java -version

# ╔════════════════ BUILD BACKEND ════════════════╗
echo "=============================="
echo "1️⃣ Building CORE (backend)"
echo "=============================="
cd "$CORE_JAVA_DIR"
mvn clean -pl module-app -am package -Dmaven.test.skip=true

CORE_JAR="$CORE_JAVA_DIR/module-app/target/module-app-1.0-SNAPSHOT.jar"
test -f "$CORE_JAR" || { echo "❌ Missing $CORE_JAR"; exit 1; }
echo "✔ Backend JAR ready"

# ╔════════════════ BUILD FRONTEND ════════════════╗
echo
echo "=============================="
echo "2️⃣ Building FRONT (UI)"
echo "=============================="
cd "$FRONT_JAVA_DIR"
mvn clean -pl "$FRONT_MODULE" -am package -Dmaven.test.skip=true

cp "$CORE_JAR" "$FRONT_TARGET/core-backend.jar"
echo "✔ Copied backend → frontend runtime"

# .env support
if [[ -f "$CORE_JAVA_DIR/.env" ]]; then
  cp "$CORE_JAVA_DIR/.env" "$FRONT_TARGET/.env"
fi

# ╔════════════════ RUNTIME IMAGE ════════════════╗
echo
echo "=============================="
echo "3️⃣ Creating Java Runtime (jlink)"
echo "=============================="

JFX_JMODS="$HOME/javafx-jmods"
test -d "$JFX_JMODS" || { echo "❌ Missing JavaFX jmods in $JFX_JMODS"; exit 1; }

RUNTIME_IMAGE="$FRONT_TARGET/runtime-$APP_NAME"
rm -rf "$RUNTIME_IMAGE"

jlink \
  --module-path "$JAVA_HOME/jmods:$JFX_JMODS" \
  --add-modules java.base,java.desktop,java.datatransfer,java.net.http,javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.swing,jdk.crypto.ec \
  --strip-debug \
  --compress=2 \
  --no-header-files \
  --no-man-pages \
  --output "$RUNTIME_IMAGE"

echo "✔ Runtime created at $RUNTIME_IMAGE"

# ╔════════════════ CROSS PLATFORM PACKAGING ════════════════╗
echo
echo "=============================="
echo "4️⃣ Packaging for: macOS | Windows | Linux"
echo "=============================="

OS=$(uname -s)

case "$OS" in
Darwin)
  echo "📦 macOS detected — creating DMG"

  ICON_PATH="$FRONT_JAVA_DIR/$FRONT_MODULE/packaging/VARTAL.icns"

  JPACKAGE_BASE_ARGS=(
    --type dmg
    --name "$APP_NAME"
    --app-version "$VERSION"
    --input "$FRONT_TARGET"
    --main-jar module-ux-1.0-SNAPSHOT.jar
    --main-class com.swe.launcher.VartalLauncher
    --runtime-image "$RUNTIME_IMAGE"
    --dest "$FRONT_DIST"
  )

  # Add icon only if file exists
  if [[ -f "$ICON_PATH" ]]; then
    echo "✔ Using custom icon"
    jpackage "${JPACKAGE_BASE_ARGS[@]}" --icon "$ICON_PATH"
  else
    echo "⚠ No icon found — packaging with default macOS icon"
    jpackage "${JPACKAGE_BASE_ARGS[@]}"
  fi
  ;;

  Linux)
    echo "📦 Linux detected — creating DEB installer"
    jpackage \
      --type deb \
      --name "$APP_NAME" \
      --app-version "$VERSION" \
      --input "$FRONT_TARGET" \
      --main-jar module-ux-1.0-SNAPSHOT.jar \
      --main-class com.swe.launcher.VartalLauncher \
      --runtime-image "$RUNTIME_IMAGE" \
      --dest "$FRONT_DIST"
  ;;
  MINGW*|CYGWIN*|MSYS*|Windows_NT)
    echo "📦 Windows detected — creating EXE"
    jpackage \
      --type exe \
      --name "$APP_NAME" \
      --app-version "$VERSION" \
      --input "$FRONT_TARGET" \
      --main-jar module-ux-1.0-SNAPSHOT.jar \
      --main-class com.swe.launcher.VartalLauncher \
      --runtime-image "$RUNTIME_IMAGE" \
      --dest "$FRONT_DIST"
  ;;
  *)
    echo "❌ Unsupported OS: $OS"
    exit 1
  ;;
esac

echo
echo "🎉 All done — universal builds ready!"
ls -1 "$FRONT_DIST"
