#!/usr/bin/env bash
# Installs the debug APK on a running emulator, launches it, and reports how far
# the port actually got. Run by .github/workflows/android-emulator.yml, which
# supplies the emulator; ANDROID_SERIAL is already pointed at it.
#
# The port has never run on hardware, so this is deliberately diagnostic rather
# than a pass/fail assertion suite: it prints the boot checkpoints that were
# reached, and fails on a crash or on not reaching the first checkpoint.
set -uo pipefail

APK=android/app/build/outputs/apk/debug/app-debug.apk
PKG=com.rs.single2012
ACTIVITY="$PKG/com.rs.android.GameActivity"
TAG=SingleRS
LOG_DIR=android/build/emulator-logs
# Cache extraction copies ~840 MB inside the emulator, which is slow.
WATCH_SECONDS="${WATCH_SECONDS:-240}"
# 1 when the APK is supposed to contain the game cache, so the run is expected
# to reach the world server rather than stopping at the missing cache.
EXPECT_FULL="${EXPECT_FULL:-0}"

mkdir -p "$LOG_DIR"

# Fail fast rather than spending fifteen minutes discovering the APK was built
# without the cache -- exactly what a falsy empty string in the workflow's build
# flag caused once already.
if [ "$EXPECT_FULL" = "1" ]; then
    if ! unzip -l "$APK" 'assets/cache/manifest.txt' >/dev/null 2>&1; then
        echo "FAIL: EXPECT_FULL is set but the APK has no assets/cache/ -- it was built without the game cache."
        exit 1
    fi
    echo "==> APK contains the bundled game cache"
fi

echo "==> Installing $(du -h "$APK" | cut -f1) APK"
if ! adb install -r -g "$APK"; then
    echo "FAIL: the APK would not install."
    exit 1
fi

adb logcat -c || true
echo "==> Launching $ACTIVITY"
adb shell am start -W -n "$ACTIVITY" || { echo "FAIL: activity would not start."; exit 1; }

# Stream logcat to a file in the background so nothing is lost if the app dies.
adb logcat -v time > "$LOG_DIR/logcat-full.txt" 2>&1 &
LOGCAT_PID=$!
# shellcheck disable=SC2064
trap "kill $LOGCAT_PID 2>/dev/null || true" EXIT

echo "==> Watching for up to ${WATCH_SECONDS}s"
deadline=$((SECONDS + WATCH_SECONDS))
while [ $SECONDS -lt $deadline ]; do
    if grep -q "FATAL EXCEPTION" "$LOG_DIR/logcat-full.txt" 2>/dev/null; then
        echo "==> Crash detected, stopping early."
        break
    fi
    # Terminal checkpoints: no point waiting out the clock after either.
    if grep -qE "$TAG.*(activity: game thread died|world server did not start|never opened port)" \
        "$LOG_DIR/logcat-full.txt" 2>/dev/null; then
        echo "==> Reached a terminal checkpoint, stopping early."
        break
    fi
    sleep 5
done

# The first frame is the loading screen; the client still has to finish loading
# and reach a login screen. Watch it for a while and capture as it goes, rather
# than screenshotting the first thing drawn and calling it rendered.
if grep -qF "first frame presented" "$LOG_DIR/logcat-full.txt" 2>/dev/null; then
    echo "==> Client is drawing; watching it settle"
    adb shell wm size || true
    for shot in 1 2 3 4; do
        sleep "${SETTLE_INTERVAL:-30}"
        adb exec-out screencap -p > "$LOG_DIR/screen-$shot.png" 2>/dev/null || true
        size=$( [ -f "$LOG_DIR/screen-$shot.png" ] && wc -c < "$LOG_DIR/screen-$shot.png" || echo 0 )
        echo "  screen-$shot.png: $size bytes"

        # The client puts an "Unsupported Java Warning" over the login box.
        # Dismiss it on the second pass so the run shows whatever is behind it,
        # whether or not the java.version fix removed it in the first place.
        if [ "$shot" = "2" ]; then
            read -r width height <<<"$(adb shell wm size | sed 's/.*: //; s/x/ /')"
            width=${width:-640}; height=${height:-320}
            tap_x=$(( width * 617 / 1000 ))
            tap_y=$(( height * 628 / 1000 ))
            echo "  tapping the dialog's right button at ${tap_x},${tap_y} (screen ${width}x${height})"
            adb shell input tap "$tap_x" "$tap_y" || true
        fi
    done
fi

sleep 2
kill $LOGCAT_PID 2>/dev/null || true

echo "==> Capturing the final screen"
adb exec-out screencap -p > "$LOG_DIR/screen.png" 2>/dev/null || echo "  (screencap failed)"
[ -s "$LOG_DIR/screen.png" ] && echo "  saved $(wc -c < "$LOG_DIR/screen.png") bytes"

grep -E "$TAG|AndroidRuntime|System\.err|art  *:|dalvik" "$LOG_DIR/logcat-full.txt" \
    > "$LOG_DIR/logcat-filtered.txt" 2>/dev/null || true

echo
echo "=================== boot checkpoints ==================="
# Ordered; each line is emitted by GameActivity or AndroidLoader.
checkpoints=(
    "activity: onCreate"
    "activity: java.awt shim loaded, Loader instantiated"
    "boot: starting, android=true"
    "boot: cache ready at"
    "boot: data ready at"
    "boot: starting world server"
    "boot: world server listening on"
    "boot: starting client engine"
    "boot: client engine started"
    "render: first frame presented"
)
reached=0
for cp in "${checkpoints[@]}"; do
    if grep -qF "$cp" "$LOG_DIR/logcat-full.txt" 2>/dev/null; then
        echo "  [reached] $cp"
        reached=$((reached + 1))
    else
        echo "  [ missed] $cp"
    fi
done

echo
echo "=================== notable log lines =================="
grep -E "FATAL EXCEPTION|VerifyError|ClassNotFoundException|NoClassDefFoundError|UnsatisfiedLinkError|Prohibited package|cache extraction failed|data extraction failed|world server failed|never opened port|game thread died" \
    "$LOG_DIR/logcat-full.txt" 2>/dev/null | head -40 || echo "  (none)"

echo
echo "=================== $TAG log ==========================="
grep -F "$TAG" "$LOG_DIR/logcat-full.txt" 2>/dev/null | head -60 || echo "  (none)"

echo
echo "=================== world server log ==================="
grep -E "System\.out|System\.err" "$LOG_DIR/logcat-full.txt" 2>/dev/null | tail -60 || echo "  (none)"

echo
echo "=================== stack traces ======================="
grep -A30 "FATAL EXCEPTION" "$LOG_DIR/logcat-full.txt" 2>/dev/null | head -60 || true
grep -A20 "game thread died" "$LOG_DIR/logcat-full.txt" 2>/dev/null | head -40 || true

echo
echo "=================== verdict ============================"
status=0
if grep -q "FATAL EXCEPTION" "$LOG_DIR/logcat-full.txt" 2>/dev/null; then
    echo "FAIL: the app crashed with a fatal exception."
    status=1
elif [ "$reached" -eq 0 ]; then
    echo "FAIL: the app never reached its first checkpoint -- it did not start."
    status=1
else
    echo "Reached $reached of ${#checkpoints[@]} boot checkpoints without a fatal exception."
    # Missing the java.awt checkpoint is the one result that matters most: it
    # means ART rejected the shim, and the whole approach needs rethinking.
    if ! grep -qF "java.awt shim loaded" "$LOG_DIR/logcat-full.txt" 2>/dev/null; then
        echo "FAIL: the java.awt shim did not load -- ART would not accept the java.* classes."
        status=1
    fi
    # With the cache bundled there is no legitimate reason to stop early.
    if [ "$EXPECT_FULL" = "1" ] && [ "$reached" -lt ${#checkpoints[@]} ]; then
        echo "FAIL: the cache is bundled, so all ${#checkpoints[@]} checkpoints were expected."
        status=1
    fi
fi
echo "Full logcat saved to $LOG_DIR/logcat-full.txt"
exit $status
