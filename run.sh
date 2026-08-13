#!/bin/sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
SAVE_DIR="$ROOT_DIR/saves"
XP_RATE=${1:-1}

if [ -f "$ROOT_DIR/world/world-server.jar" ] && [ -f "$ROOT_DIR/client.jar" ]; then
    WORLD_DIR="$ROOT_DIR/world"
    WORLD_JAR="$WORLD_DIR/world-server.jar"
    CLIENT_JAR="$ROOT_DIR/client.jar"
    CACHE_DIR="$ROOT_DIR/cache"
    JAVA_BIN="$ROOT_DIR/runtime/bin/java"
else
    WORLD_DIR="$ROOT_DIR/darkan-world-server"
    WORLD_JAR="$WORLD_DIR/build/libs/world-server-2.0.12-all.jar"
    CLIENT_JAR="$ROOT_DIR/darkan-client/build/libs/darkan-client-shaded-1.0.1-all.jar"
    CACHE_DIR="$ROOT_DIR/darkan-cache"
    if [ -x "$WORLD_DIR/.jdk24/bin/java" ]; then
        JAVA_BIN="$WORLD_DIR/.jdk24/bin/java"
    elif [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_BIN="$JAVA_HOME/bin/java"
    else
        JAVA_BIN=java
    fi
fi

case "$XP_RATE" in
    ''|*[!0-9]*) echo "Usage: ./run.sh [xp rate]   e.g. ./run.sh (1x), ./run.sh 25, ./run.sh 50" >&2; exit 2 ;;
    0) echo "The XP rate must be at least 1." >&2; exit 2 ;;
esac

if [ ! -f "$WORLD_JAR" ] || [ ! -f "$CLIENT_JAR" ]; then
    echo "Offline game jars are missing. Run ./build-offline.sh first." >&2
    exit 1
fi
if [ ! -x "$JAVA_BIN" ]; then
    echo "The Java runtime is missing or cannot be executed: $JAVA_BIN" >&2
    exit 1
fi
if [ ! -f "$CACHE_DIR/main_file_cache.dat2" ]; then
    echo "The Darkan cache is missing from $CACHE_DIR." >&2
    exit 1
fi

mkdir -p "$SAVE_DIR"
cd "$WORLD_DIR"
exec "$JAVA_BIN" -Xmx3g \
    -Ddarkan.singlePlayer=true \
    -Ddarkan.cache.path="$CACHE_DIR" \
    -Ddarkan.save.path="$SAVE_DIR" \
    -Ddarkan.xpRate="$XP_RATE" \
    -cp "$WORLD_JAR:$CLIENT_JAR" \
    com.rs.SinglePlayerLauncher
