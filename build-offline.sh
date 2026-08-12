#!/bin/sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WORLD_DIR="$ROOT_DIR/darkan-world-server"
CLIENT_DIR="$ROOT_DIR/darkan-client"
GRADLE_DIR="$WORLD_DIR/.gradle-user-home"

if [ -x "$WORLD_DIR/.jdk24/bin/java" ]; then
    BUILD_JAVA_HOME="$WORLD_DIR/.jdk24"
elif [ -n "${JAVA_HOME:-}" ]; then
    BUILD_JAVA_HOME="$JAVA_HOME"
else
    echo "JDK 24 is required. Set JAVA_HOME or install it in darkan-world-server/.jdk24." >&2
    exit 1
fi

(cd "$CLIENT_DIR" && JAVA_HOME="$BUILD_JAVA_HOME" GRADLE_USER_HOME="$GRADLE_DIR" ./gradlew clean shadowJar)
(cd "$WORLD_DIR" && JAVA_HOME="$BUILD_JAVA_HOME" GRADLE_USER_HOME="$GRADLE_DIR" ./gradlew clean shadowJar)
echo "Build complete. Start with ./run.sh 25 or ./run.sh 50."
