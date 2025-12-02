#!/bin/bash
set -euo pipefail

###############################################################################
# Basic config
###############################################################################
APP_NAME="VARTALA"
VERSION="1.0.0"

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
CORE_DIR="$BASE_DIR/java-core"
FRONT_DIR="$BASE_DIR/java-front"

CORE_MODULE_APP="$CORE_DIR/module-app"
FRONT_MODULE_UX="$FRONT_DIR/module-ux"

CORE_TARGET="$CORE_MODULE_APP/target"
FRONT_TARGET="$FRONT_MODULE_UX/target"
DIST_DIR="$FRONT_MODULE_UX/dist"

echo "===================================================="
echo "  🔧 $APP_NAME — Full Build & Packaging"
echo "===================================================="
echo "➡ Base dir     : $BASE_DIR"
echo "➡ Backend root : $CORE_DIR"
echo "➡ Frontend root: $FRONT_DIR"
echo

###############################################################################
# Helper: command presence
###############################################################################
need_cmd() {
  if ! command -v "$1" &>/dev/null; then
    echo "❌ Missing required command: $1"
    exit 1
  fi
}

need_cmd mvn
need_cmd java

echo "➡ Java version:"
java -version 2>&1 | head -n 2
echo

###############################################################################
# 1️⃣ Build backend (java-core/module-app → module-app-1.0-SNAPSHOT.jar)
###############################################################################
echo "=============================="
echo "1) 🧱 Building backend (core)"
echo "=============================="

cd "$CORE_DIR"

# Build module-app and everything it needs, skip tests
mvn clean -pl module-app -am package -DskipTests -Dmaven.test.skip=true

CORE_JAR="$CORE_TARGET/module-app-1.0-SNAPSHOT.jar"
if [[ ! -f "$CORE_JAR" ]]; then
  echo "❌ Backend jar not found at: $CORE_JAR"
  exit 1
fi
echo "✔ Backend jar built: $CORE_JAR"
echo

###############################################################################
# 2️⃣ Build frontend (java-front/module-ux → shaded module-ux-1.0-SNAPSHOT.jar)
###############################################################################
echo "=============================="
echo "2) 🎨 Building frontend (UI)"
echo "=============================="

cd "$FRONT_DIR"

# Build module-ux and its deps, skip tests / coverage
mvn clean -pl module-ux -am package -DskipTests -Dmaven.test.skip=true -Djacoco.skip=true

FRONT_JAR="$FRONT_TARGET/module-ux-1.0-SNAPSHOT.jar"
if [[ ! -f "$FRONT_JAR" ]]; then
  echo "❌ Frontend jar not found at: $FRONT_JAR"
  exit 1
fi
echo "✔ Frontend jar built: $FRONT_JAR"

# Put backend next to UI — launcher expects core-backend.jar beside UI jar
cp "$CORE_JAR" "$FRONT_TARGET/core-backend.jar"
echo "✔ Copied backend → $FRONT_TARGET/core-backend.jar"

# Optional: copy .env from java-core root if present
if [[ -f "$CORE_DIR/.env" ]]; then
  cp "$CORE_DIR/.env" "$FRONT_TARGET/.env"
  echo "✔ Copied .env → $FRONT_TARGET/.env"
else
  echo "⚠ No .env found at $CORE_DIR/.env (will rely on real env vars)"
fi
echo

###############################################################################
# 3️⃣ Try to create custom runtime (jlink) — optional
###############################################################################
echo "=============================="
echo "3) 🧱 Creating custom Java runtime (jlink)"
echo "=============================="

JLINK_OK=true

# 3.1 jlink presence
if ! command -v jlink &>/dev/null; then
  echo "⚠ jlink not found on PATH — skipping runtime image."
  JLINK_OK=false
fi

# 3.2 Find JavaFX JMODs
detect_jfx_jmods() {
  for base in "$HOME/javafx-jmods" "$HOME/javafx-jmods-24" "$HOME/javafx" ; do
    if [[ -d "$base" ]]; then
      # Either the directory itself has jmods, or one subdir does
      if ls "$base"/javafx*.jmod &>/dev/null; then
        echo "$base"
        return 0
      fi
      for d in "$base"/*; do
        if [[ -d "$d" ]] && ls "$d"/javafx*.jmod &>/dev/null; then
          echo "$d"
          return 0
        fi
      done
    fi
  done
  return 1
}

JFX_JMODS=""
if $JLINK_OK; then
  if JFX_JMODS="$(detect_jfx_jmods)"; then
    echo "✔ JavaFX JMODs found at: $JFX_JMODS"
  else
    echo "⚠ Could not locate JavaFX JMODs under \$HOME. Skipping jlink runtime."
    JLINK_OK=false
  fi
fi

# 3.3 Build runtime if we have everything
RUNTIME_IMAGE="$FRONT_TARGET/runtime-$APP_NAME"
if $JLINK_OK; then
  if [[ -z "${JAVA_HOME:-}" || ! -d "$JAVA_HOME/jmods" ]]; then
    echo "⚠ JAVA_HOME/jmods not usable; set JAVA_HOME to a full JDK (not JRE) to enable jlink."
    JLINK_OK=false
  fi
fi

if $JLINK_OK; then
  echo "➡ Using JAVA_HOME jmods: $JAVA_HOME/jmods"
  echo "➡ Output runtime:        $RUNTIME_IMAGE"
  rm -rf "$RUNTIME_IMAGE"

  jlink \
    --module-path "$JAVA_HOME/jmods:$JFX_JMODS" \
    --add-modules java.base,java.desktop,java.datatransfer,java.logging,java.net.http,javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.swing \
    --strip-debug \
    --compress=2 \
    --no-header-files \
    --no-man-pages \
    --output "$RUNTIME_IMAGE"

  echo "✔ Runtime image created at: $RUNTIME_IMAGE"
else
  echo "ℹ Skipping runtime creation. You can still run with system JDK:"
  echo "   cd \"$FRONT_TARGET\" && java -jar module-ux-1.0-SNAPSHOT.jar"
fi
echo

###############################################################################
# 4️⃣ Try to package installer (jpackage) — optional
###############################################################################
echo "=============================="
echo "4) 📦 Packaging installer (jpackage)"
echo "=============================="

mkdir -p "$DIST_DIR"

if ! command -v jpackage &>/dev/null; then
  echo "⚠ jpackage not found — skipping installer packaging."
else
  # Decide package type by OS
  OS="$(uname -s || echo "Unknown")"
  case "$OS" in
    Darwin*) PKG_TYPE="dmg" ;;
    Linux*)  PKG_TYPE="deb" ;;     # tweak if you prefer rpm
    MINGW*|MSYS*|CYGWIN*) PKG_TYPE="exe" ;;
    *) PKG_TYPE="app-image" ;;
  esac

  JPACKAGE_ARGS=(
    --type "$PKG_TYPE"
    --name "$APP_NAME"
    --app-version "$VERSION"
    --input "$FRONT_TARGET"
    --main-jar "$(basename "$FRONT_JAR")"
    --dest "$DIST_DIR"
  )

  # If we have a custom runtime from jlink, use it
  if [[ -d "$RUNTIME_IMAGE" ]]; then
    JPACKAGE_ARGS+=( --runtime-image "$RUNTIME_IMAGE" )
  fi

  # Optional: icon support (if you later drop one in)
  ICON_CANDIDATE="$FRONT_MODULE_UX/packaging/${APP_NAME}.icns"
  if [[ -f "$ICON_CANDIDATE" ]]; then
    JPACKAGE_ARGS+=( --icon "$ICON_CANDIDATE" )
  fi

  echo "➡ Running jpackage (type=$PKG_TYPE)..."
  jpackage "${JPACKAGE_ARGS[@]}"
  echo "✔ Installer(s) created under: $DIST_DIR"
fi
echo

###############################################################################
# 5️⃣ Summary + how to run
###############################################################################
echo "=============================="
echo "5) ✅ Summary"
echo "=============================="
echo "✔ Backend jar : $CORE_JAR"
echo "✔ Frontend jar: $FRONT_JAR"
echo "✔ Front target: $FRONT_TARGET"
if [[ -d "$RUNTIME_IMAGE" ]]; then
  echo "✔ Runtime     : $RUNTIME_IMAGE"
fi
if [[ -d "$DIST_DIR" ]]; then
  echo "📦 Dist       : $DIST_DIR"
fi

echo
echo "👉 To run from terminal (using system JDK):"
echo "   cd \"$FRONT_TARGET\""
echo "   java -jar module-ux-1.0-SNAPSHOT.jar"
echo
echo "   (Your VartalLauncher inside that jar should start the backend"
echo "    by running core-backend.jar after a delay.)"
echo
echo "🎉 Done!"
