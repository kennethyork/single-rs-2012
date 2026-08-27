package com.rs.android;

import android.util.Log;

import com.rs.Loader;
import com.rs.jagex.client;

/**
 * Entry point for the 2012 client on Android.
 *
 * Mirrors the desktop Loader's boot sequence (loadParams -> new client ->
 * supplyApplet -> init -> start) but hosts rendering through the Android
 * GameSurfaceView instead of an AWT Frame. The safe-mode JavaRenderer produces
 * the int[] ARGB frame that GameSurfaceView renders to the screen.
 */
public final class AndroidLoader {

    /**
     * Boot checkpoints are logged under one tag. There is no debugger on a CI
     * emulator, so logcat is the only way to see how far the port got; the
     * emulator test in .github/workflows asserts on these lines.
     */
    public static final String TAG = "SingleRS";

    private AndroidLoader() {}

    /** Boots the client engine against the supplied headless Loader host. */
    public static void boot(Loader loader) {
        // Android has no OpenGL/DirectX native libraries: force the safe-mode
        // JavaRenderer (toolkit 0). Checked by GraphicsToolkitPreference.
        Log.i(TAG, "boot: starting, android=true");
        System.setProperty("darkan.android", "true");
        // Extract the game cache from assets into writable storage (the client
        // and the server's rs.darkan:core both open it with RandomAccessFile,
        // so it must be on disk). On the first launch this copies ~840 MB and
        // takes a minute or two, so report progress rather than sitting on a
        // black screen.
        try {
            AndroidPlatform.extractCacheIfNeeded(AndroidLoader::reportCacheProgress);
        } catch (java.io.IOException e) {
            Log.e(TAG, "boot: cache extraction failed", e);
            showStatus("Could not unpack the game cache: " + e.getMessage());
            return;
        }
        Log.i(TAG, "boot: cache ready at " + AndroidPlatform.getCacheDir());
        System.setProperty("darkan.cache.path", AndroidPlatform.getCacheDir());
        Loader.loadParams();
        if (Loader.IP_ADDRESS == null) Loader.IP_ADDRESS = "127.0.0.1";
        Log.i(TAG, "boot: starting client engine");
        client clnt = new client();
        clnt.supplyApplet(loader);
        clnt.init();
        clnt.start();
        Log.i(TAG, "boot: client engine started");
    }

    private static int lastReportedPercent = -1;

    private static void reportCacheProgress(long bytesDone, long bytesTotal) {
        if (bytesTotal <= 0) return;
        int percent = (int) (bytesDone * 100 / bytesTotal);
        // setPixels-free redraw, but still a surface lock: only repaint on change.
        if (percent == lastReportedPercent) return;
        lastReportedPercent = percent;
        showStatus("Unpacking the game cache\u2026 " + percent + "%");
    }

    private static void showStatus(String message) {
        GameSurfaceView view = GameBridge.surfaceView;
        if (view != null) view.drawStatus(message);
    }

    /**
     * Called by the software renderer (Class158_Sub2_Sub3_Sub1.method14353)
     * each time a frame is presented, to push the ARGB pixel buffer to the
     * Android surface.
     */
    public static void presentFrame(int[] pixels, int width, int height) {
        GameSurfaceView v = GameBridge.surfaceView;
        if (v != null) {
            v.renderFrame(pixels, width, height);
        }
    }
}
